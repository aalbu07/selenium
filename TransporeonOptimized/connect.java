// AWS SDK v2 imports
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.core.sync.ResponseInputStream

// Apache Commons Lang
import org.apache.commons.lang3.StringUtils

// Java standard library
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.List
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
    // Create the S3 client
    S3Client s3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build()

    // List files in the bucket
    ListObjectsV2Request listObjectsReq = ListObjectsV2Request.builder()
        .bucket(bucketName)
        .prefix(bucketPath)
        .build()
    
    def listObjectsRes = s3Client.listObjectsV2(listObjectsReq)

    // Filter and sort files
    def matchingFiles = listObjectsRes.contents().stream()
        .filter { it.key().matches(/SPMHARP1\d{12}\.txt/) }
        .sorted((a, b) -> {
            def dateA = a.key().replaceAll(/[^\d]/, "").substring(8, 20)
            def dateB = b.key().replaceAll(/[^\d]/, "").substring(8, 20)
            def format = new SimpleDateFormat("yyyyMMddHHmm")
            return format.parse(dateB) <=> format.parse(dateA)
        })
        .collect()

    if (matchingFiles.isEmpty()) {
        log.error("No matching files found in the S3 bucket.")
        assert false : "No matching files found."
    }

    // Get the key of the latest file
    def latestFileKey = matchingFiles[0].key()
    log.info("Latest file found: " + latestFileKey)

    // Download the S3 object into a buffered reader for in-memory storage
    ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject({ it.bucket(bucketName).key(latestFileKey) })
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

aws-java-sdk-s3-2.20.100.jar
aws-java-sdk-core-2.20.100.jar
aws-java-sdk-sts-2.20.100.jar
software.amazon.awssdk.auth-2.20.100.jar
software.amazon.awssdk.regions-2.20.100.jar
software.amazon.awssdk.http-client-spi-2.20.100.jar
software.amazon.awssdk.http-url-connection-2.20.100.jar
log4j-api-2.20.100.jar
log4j-core-2.20.100.jar
javax.net.ssl.jar
slf4j-api-1.7.36.jar
commons-io-2.11.0.jar
commons-lang3-3.12.0.jar
jackson-core-2.15.0.jar
jackson-databind-2.15.0.jar
jackson-annotations-2.15.0.jar
joda-time-2.10.14.jar
guava-31.1-jre.jar
junit-4.13.2.jar
httpclient-4.5.14.jar
httpcore-4.4.16.jar

    import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
