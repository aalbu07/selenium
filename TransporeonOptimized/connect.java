import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ListObjectsV2Request
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectInputStream
import java.text.SimpleDateFormat
import java.util.stream.Collectors

// Fetch properties from ReadyAPI project properties
def bucketName = context.expand('${#Project#bucketName}')
def bucketPath = context.expand('${#Project#bucketPath}')
def certBundlePath = context.expand('${#Project#certBundlePath}')
def certBundlePass = context.expand('${#Project#certBundlePass}')
def region = context.expand('${#Project#region}')

// Set up the SSL trust store
System.setProperty("javax.net.ssl.trustStore", certBundlePath)
System.setProperty("javax.net.ssl.trustStorePassword", certBundlePass)

try {
    // Create the S3 client using AWS SDK v1.12.177
    AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
        .withRegion(region)
        .withCredentials(new DefaultAWSCredentialsProviderChain())
        .build()
  // Check if the connection to S3 is successful by performing a simple operation
    if (s3Client.doesBucketExistV2(bucketName)) {
        log.info("Successfully connected to S3 bucket: " + bucketName)
    } else {
        log.error("Failed to connect to S3 bucket: " + bucketName)
        assert false : "Bucket does not exist or connection failed."
    }
    // List files in the bucket
    ListObjectsV2Request listObjectsReq = new ListObjectsV2Request()
        .withBucketName(bucketName)
        .withPrefix(bucketPath)

    def listObjectsRes = s3Client.listObjectsV2(listObjectsReq)

    // Filter and sort files
    def matchingFiles = listObjectsRes.getObjectSummaries().stream()
        .filter { it.getKey().matches(/SPMHARP1\d{12}\.txt/) }
        .sorted { a, b ->
            def dateA = a.getKey().replaceAll(/[^\d]/, "").substring(8, 20)
            def dateB = b.getKey().replaceAll(/[^\d]/, "").substring(8, 20)
            def format = new SimpleDateFormat("yyyyMMddHHmm")
            return format.parse(dateB) <=> format.parse(dateA)
        }
        .collect(Collectors.toList())

    if (matchingFiles.isEmpty()) {
        log.error("No matching files found in the S3 bucket.")
        assert false : "No matching files found."
    }

    // Get the key of the latest file
    def latestFileKey = matchingFiles.get(0).getKey()
    log.info("Latest file found: " + latestFileKey)

    // Download the S3 object into a buffered reader for in-memory storage
    S3Object s3Object = s3Client.getObject(bucketName, latestFileKey)
    S3ObjectInputStream s3ObjectStream = s3Object.getObjectContent()
    BufferedReader reader = new BufferedReader(new InputStreamReader(s3ObjectStream))

    // Read the file content line by line into a List for easier manipulation
    List<String> fileLines = []
    String line
    while ((line = reader.readLine()) != null) {
        fileLines.add(line)
    }
    reader.close()

    // Save the file lines to a context property for further processing in another script
    context.setProperty("DownloadedFileLines", fileLines)
    log.info("File content downloaded and stored in context property as a list of lines.")

} catch (Exception e) {
    log.error("An error occurred: " + e.getMessage(), e)
    assert false : "Test failed due to an exception: ${e.message}"
}
