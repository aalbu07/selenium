package old;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.simple.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Standalone {

	public static void main(String[] args) throws InterruptedException {
		System.setProperty("webdriver.chrome.driver", "C:\\Alexei\\Automation\\chromedriver_win32\\chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		// Configure Chrome options
		// ChromeOptions options = new ChromeOptions();
		// options.addArguments("--headless");
		driver.manage().window().maximize();
		driver.get("https://github.com/SeleniumHQ/selenium");
		// 1.a Open the dialog “Code”
		driver.findElement(By.xpath("//summary[@class='Button--primary Button--medium Button flex-1 d-inline-flex']"))
				.click();
		// 1.b Get the repository clone URL from the input field
		wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector("clipboard-copy[value*='selenium.git']")));
		driver.findElement(By.cssSelector("clipboard-copy[value*='selenium.git']")).click();
		Thread.sleep(2000);
		String clipboardText = getClipboardText();
		System.out.println("repository URL: " + clipboardText);
		// 1.c Get the number of releases

		WebElement releases = driver.findElement(By.xpath("//span[@title='83']"));
		String releasesNr = releases.getText();
		System.out.println("number of releases: " + releasesNr);

		// 1.d Create a JSON object and store the text value
		JSONObject jsonObject1 = new JSONObject();
		jsonObject1.put("releases", releasesNr);

		// Write JSON object to a file
		try (FileWriter fileWriter = new FileWriter("selenium-meta-data.json")) {
			fileWriter.write(jsonObject1.toJSONString());
			System.out.println("Data is saved to selenium-meta-data.json successfully!");
		} catch (IOException e) {
			System.out.println("error occurred while writing to selenium-meta-data.json: " + e.getMessage());
		}

		////// Part2 //////
		System.out.println("******** The 2nd part of the test  *******  ");
		// Click on tab “Issues”
		WebElement issues = driver.findElement(By.id("issues-tab"));
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		executor.executeScript("arguments[0].click();", issues);

		// 2.a Clear search field and type “sort:comments-desc”
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("js-issues-search")));
		WebElement inputElement = driver.findElement(By.id("js-issues-search"));
		inputElement.clear();
		inputElement.sendKeys("sort:comments-desc");
		inputElement.sendKeys(Keys.ENTER);
		// wait until the page is refreshed after pressing ENTER
		wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.id("js-issues-search"))));

		// 2.b Filter by label “C-java”
		driver.findElement(By.id("label-select-menu")).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'C-java')]")));
		driver.findElement(By.xpath("//div[contains(text(),'C-java')]")).click();
		wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.id("js-issues-search"))));
		
		// 2.c press Open button
		driver.findElement(By.xpath("//div[contains(@class, 'd-lg-block')]/div/a[1]")).click();
		wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.cssSelector("div[aria-label='Issues'] div[class*='js-navigation-container']"))));
		
		// 2.d Get the following attributes of the first (most top) issue displayed
		// limiting the driver scope to the container div
		WebElement containerDiv = driver
				.findElement(By.cssSelector("div[aria-label='Issues'] div[class*='js-navigation-container']"));
		// limiting the driver scope to the first (most top) issue
		WebElement issueRow = containerDiv.findElement(By.cssSelector("div[class*='Box-row ']:nth-child(1)"));

		String title = issueRow.findElement(By.cssSelector("div div a[class*='Link--primary']")).getText();
		System.out.println("issue title : " + title);

		// printing all labels
		List<WebElement> labels = issueRow.findElements(By.cssSelector("div span a[class*='IssueLabel']"));

		for (WebElement label : labels) {
			String labelText = label.getText();
			System.out.println("Label Text: " + labelText);
		}

		// printing nr of comments
		String nr_comments = issueRow
				.findElement(By.cssSelector("div div[class*='flex-shrink-0']  span[class*='flex-1'] a span")).getText();
		System.out.println("number of comments : " + nr_comments);

		// String value is: #9845 opened on Sep 19, 2021Sep 19, 2021 by gazal-k

		String issueID = issueRow.findElement(By.cssSelector("div div span[class='opened-by']")).getText();
		// extract the issue ID and username using split()
		String[] splitIssueID = issueID.split(" ");
		String extractedIssueID = splitIssueID[0];
		String extractedUsername = splitIssueID[splitIssueID.length - 1];
		System.out.println("issueID: " + extractedIssueID + " " + "author: " + extractedUsername);

		String datetimeValue = datetimeValue = issueRow.findElement(By.cssSelector("div div relative-time"))
				.getAttribute("datetime");
		// Parse the datetime string
		LocalDateTime datetime = LocalDateTime.parse(datetimeValue, DateTimeFormatter.ISO_DATE_TIME);

		// Format the datetime as desired
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		String formattedDateTime = datetime.format(formatter);

		//formattedDateTime = formattedDateTime.replace("\\", "");

		// Print the formatted datetime
		System.out.println("Date/Time: " + formattedDateTime);

		// 2.d Write the information gathered represented as json to a file
		// most-discussed-java-issue.json
		JSONObject jsonObject2 = new JSONObject();
		jsonObject2.put("title", title);
		// label are multipe, so we call this method below
		jsonObject2.put("number of comments", nr_comments);
		jsonObject2.put("issue id", extractedIssueID);
		jsonObject2.put("author", extractedUsername);
		jsonObject2.put("created", formattedDateTime);

		JSONObject labelsObject = new JSONObject();

		for (int i = 0; i < labels.size(); i++) {
			WebElement element = labels.get(i);
			String labelText = element.getText();

			// Add the labelText value to the labelsObject
			labelsObject.put("label" + (i + 1), labelText);
		}

		// Add the labelsObject to the jsonObject with the key "labels"
		jsonObject2.put("labels", labelsObject);
		
		    Gson gson = new GsonBuilder().setPrettyPrinting().create();
	        String jsonStringPretty = gson.toJson(jsonObject2);

		// Write JSON object to a file
		try (FileWriter fileWriter = new FileWriter("most-discussed-java-issue.json")) {
			fileWriter.write(jsonStringPretty);
			System.out.println("Data is saved to most-discussed-java-issue.json successfully!");
		} catch (IOException e) {
			System.out.println("error occurred while writing to most-discussed-java-issue.json: " + e.getMessage());
		}

		Thread.sleep(2000);
		driver.quit();
	}

	public static String getClipboardText() {
		try {
			return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
