import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.auth.credentials.WebIdentityTokenCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.services.s3.model.S3Object
import java.nio.charset.StandardCharsets
import java.io.InputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat

// Fetch properties from ReadyAPI project properties
def bucketName = context.expand('${#Project#bucketName}')
def bucketPath = context.expand('${#Project#bucketPath}')
def certBundlePath = context.expand('${#Project#certBundlePath}')
def certBundlePass = context.expand('${#Project#certBundlePass}')
def regionName = context.expand('${#Project#region}')

// Set up the SSL trust store
System.setProperty("javax.net.ssl.trustStore", certBundlePath)
System.setProperty("javax.net.ssl.trustStorePassword", certBundlePass)

try {
    // Determine the credentials provider based on the user’s home directory
    def credentialsProvider
    if (System.getProperty("os.name").toLowerCase().contains("windows")) { // Assuming this is for Windows
        // Use the default credentials provider (AWS CLI configured credentials)
        credentialsProvider = ProfileCredentialsProvider.create()
    } else {
        // For non-Windows environments, use WebIdentityTokenCredentialsProvider (e.g., for EKS)
        credentialsProvider = WebIdentityTokenCredentialsProvider.create()
    }

    // Create the S3 client using the determined credentials provider and specified region
    Region region = Region.of(regionName)
    S3Client s3Client = S3Client.builder()
        .credentialsProvider(credentialsProvider)
        .region(region)
        .build()

    // List all files matching the pattern SPMHARP1%s.txt
    ListObjectsV2Request listObjectsReq = ListObjectsV2Request.builder()
        .bucket(bucketName)
        .prefix(bucketPath)
        .build()
    ListObjectsV2Response listObjectsRes = s3Client.listObjectsV2(listObjectsReq)

    // Filter the files by prefix and date format
    def matchingFiles = listObjectsRes.contents().findAll { it.key().matches(/.*SPMHARP1\d{12}\.txt/) }

    if (matchingFiles.isEmpty()) {
        throw new FileNotFoundException("No matching files found in S3 bucket.")
    }

    // Sort files by the date extracted from the file name (descending order to get the latest)
    def sortedFiles = matchingFiles.sort { a, b ->
        def dateA = a.key().replaceAll(/[^\d]/, "").substring(8, 20)
        def dateB = b.key().replaceAll(/[^\d]/, "").substring(8, 20)
        def format = new SimpleDateFormat("yyMMddHHmmss")
        return format.parse(dateB) <=> format.parse(dateA)
    }

    // Get the key of the latest file
    def latestFileKey = sortedFiles[0].key()
    log.info("Latest file found: " + latestFileKey)

    // Get the S3 object
    GetObjectRequest getObjectReq = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(latestFileKey)
        .build()
    InputStream s3ObjectInputStream = s3Client.getObject(getObjectReq)

    // Read the file content directly into memory
    BufferedReader reader = new BufferedReader(new InputStreamReader(s3ObjectInputStream, StandardCharsets.UTF_8))
    StringBuilder fileContent = new StringBuilder()
    String line
    while ((line = reader.readLine()) != null) {
        fileContent.append(line).append("\n")
    }
    reader.close()

    // Save the file content to a context property for further processing in another script
    context.setProperty("DownloadedFileContent", fileContent.toString())
    log.info("File content downloaded and stored in context property.")

} catch (FileNotFoundException e) {
    log.error("File not found: " + e.getMessage())
} catch (Exception e) {
    if (e.message.contains("credentials")) {
        log.error("Incorrect AWS credentials: " + e.getMessage())
    } else {
        log.error("An error occurred: " + e.getMessage())
    }
    assert false : "Test failed due to an exception: ${e.message}"
}









aws-sdk-java-core 2.20.45 JAR download
aws-sdk-java-s3 2.20.45 JAR download
aws-sdk-java-utils 2.20.45 JAR download
aws-sdk-java-auth 2.20.45 JAR download
apache httpclient 5.2.1 JAR download
apache httpcore 5.2.1 JAR download
jackson annotations 2.15.2 JAR download
jackson core 2.15.2 JAR download
jackson databind 2.15.2 JAR download
slf4j api 1.7.36 JAR download
slf4j simple 1.7.36 JAR download
netty all 4.1.97.Final JAR download
commons logging 1.2 JAR download


netty-all-4.x.x.jar (Used for non-blocking networking)
Commons Logging (if needed by other dependencies):

commons-logging-1.2.jar