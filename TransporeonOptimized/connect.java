import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.WebIdentityTokenCredentialsProvider
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import java.text.SimpleDateFormat
import java.io.BufferedReader
import java.io.InputStreamReader

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
    // Determine the credentials provider based on the user’s home directory
    def credentialsProvider
    if (System.getProperty("user.home").split("/")[1] == "Users") { // Assuming this is for Windows
        // Use the default credentials provider (AWS CLI configured credentials)
        credentialsProvider = new ProfileCredentialsProvider()
    } else {
        // For non-Windows environments, use WebIdentityTokenCredentialsProvider (e.g., for EKS)
        credentialsProvider = WebIdentityTokenCredentialsProvider.create()
    }

    // Create the S3 client using the determined credentials provider
    AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
        .withCredentials(credentialsProvider)
        .withRegion(region)
        .build()

    // List all files matching the pattern SPMHARP1%s.txt
    def objectSummaries = s3Client.listObjects(bucketName, bucketPath).getObjectSummaries()

    // Filter the files by prefix and date format
    def matchingFiles = objectSummaries.findAll { it.key =~ /SPMHARP1\d{12}\.txt/ }

    // Sort files by the date extracted from the file name (descending order to get the latest)
    def sortedFiles = matchingFiles.sort { a, b ->
        def dateA = a.key.replaceAll(/[^\d]/, "").substring(8, 20)
        def dateB = b.key.replaceAll(/[^\d]/, "").substring(8, 20)
        def format = new SimpleDateFormat("yyMMddHHmmss")
        return format.parse(dateB) <=> format.parse(dateA)
    }

    if (sortedFiles.isEmpty()) {
        throw new FileNotFoundException("No matching files found in S3 bucket.")
    }

    // Get the key of the latest file
    def latestFileKey = sortedFiles[0].key
    log.info("Latest file found: " + latestFileKey)

    // Get the S3 object
    S3Object latestFile = s3Client.getObject(bucketName, latestFileKey)

    // Read the file content directly into memory
    BufferedReader reader = new BufferedReader(new InputStreamReader(latestFile.getObjectContent(), 'UTF-8'))
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
