// Retrieve the file content stored in the context property by the first script
def fileContents = context.getProperty("DownloadedFileContent")

if (!fileContents) {
    log.error("No file content found in context. Ensure the S3 download script executed correctly.")
    assert false : "Test failed due to missing file content."
}

try {
    // Parse the file content: Check if field number 2, starting from position 5, until position 10, has "00000"
    def fieldStartPos = 5 - 1 // Adjust for 0-based index
    def fieldEndPos = 10

    // Loop through each line and check the specified field content
    boolean fieldCheckPassed = false
    def lines = fileContents.split("\n")
    lines.eachWithIndex { line, index ->
        if (line.length() >= fieldEndPos) { // Ensure the line has enough length
            def fieldToCheck = line.substring(fieldStartPos, fieldEndPos)
            if (fieldToCheck == "00000") {
                log.info("Field check passed on line ${index + 1}: The content is '00000'")
                fieldCheckPassed = true
                return // Exit loop if match found
            }
        }
    }

    if (!fieldCheckPassed) {
        log.error("Field check failed: No lines matched the criteria '00000' at the specified position")
        assert false : "Field validation failed: No matching content found in file"
    }

} catch (Exception e) {
    log.error("An error occurred while parsing the file content: " + e.getMessage())
    assert false : "Test failed due to an exception during parsing: ${e.message}"
}
