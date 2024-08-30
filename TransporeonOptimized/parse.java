// Fetch the downloaded file content from the context
def fileContent = context.getProperty("DownloadedFileContent")

if (!fileContent) {
    log.error("No file content found in the context. Make sure the file is downloaded properly in the previous step.")
    assert false : "Test failed because no file content is available."
}

// Example: Parse the file content
def lines = fileContent.split("\n")

// Perform checks on specific fields
lines.each { line ->
    // Example check: Ensure that field number 2, starting from position 5, until position 10, has only "00000"
    def fieldNumber = 2
    def startPosition = 5
    def endPosition = 10

    if (line.length() >= endPosition) {
        def fieldContent = line.substring(startPosition, endPosition)
        if (fieldContent != "00000") {
            log.error("Validation failed for line: '${line}' - Expected '00000' at positions 5-10, found '${fieldContent}'")
            assert false : "Test failed due to validation error."
        }
    } else {
        log.warn("Skipping line due to insufficient length: '${line}'")
    }
}

log.info("File parsing and validation completed successfully.")
