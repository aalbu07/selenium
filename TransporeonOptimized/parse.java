// Retrieve the downloaded file lines from context property set in Step 1
List<String> fileLines = context.getProperty("DownloadedFileLines")

// Ensure the file lines are not null or empty
if (fileLines == null || fileLines.isEmpty()) {
    log.error("File content is not available or is empty.")
    assert false : "No file content found for parsing."
}

try {
    // Iterate over each line to validate the specific field
    boolean allMatch = true
    fileLines.each { line ->
        // Ensure line length is sufficient for the expected field
        if (line.length() >= 10) {
            String field = line.substring(4, 10) // Extract characters from position 5 to 10
            if (!field.equals("00000")) {
                log.error("Line does not match the required pattern: ${line}")
                allMatch = false
            }
        } else {
            log.warn("Line is too short to contain the field: ${line}")
        }
    }

    // Assert that all lines match the condition
    assert allMatch : "One or more lines do not have '00000' in the specified field."

    log.info("All lines have the correct field value.")

} catch (Exception e) {
    log.error("An error occurred during parsing: " + e.getMessage(), e)
    assert false : "Test failed due to an exception during parsing: ${e.message}"
}
