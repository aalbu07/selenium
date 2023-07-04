package homework;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class IssuesPage extends AbstractComponents {

	WebDriver driver;

	public IssuesPage(WebDriver driver) {
		super(driver);
		this.driver = driver;
		PageFactory.initElements(driver, this);

	}

	// PageFactory

	@FindBy(id = "issues-tab")
	WebElement issuesTab;

	@FindBy(id = "js-issues-search")
	WebElement searchField;

	@FindBy(id = "label-select-menu")
	WebElement labelSelectMenue;

	@FindBy(xpath = "//div[contains(text(),'C-java')]")
	WebElement label;

	@FindBy(xpath = "//div[contains(@class, 'd-lg-block')]/div/a[1]")
	WebElement openButton;

	@FindBy(css = "div[aria-label='Issues'] div[class*='js-navigation-container'] div[class*='Box-row ']:nth-child(1) div div a[class*='Link--primary']")
	WebElement title;

	@FindBy(css = "div[aria-label='Issues'] div[class*='js-navigation-container'] div[class*='Box-row ']:nth-child(1) div div[class*='flex-shrink-0'] span[class*='flex-1'] a span")
	WebElement comments;

	@FindBy(css = "div[aria-label='Issues'] div[class*='js-navigation-container'] div[class*='Box-row ']:nth-child(1) div div span[class='opened-by']")
	WebElement issueID;

	@FindBy(css = "div[aria-label='Issues'] div[class*='js-navigation-container'] div[class*='Box-row ']:nth-child(1) div div relative-time")
	WebElement date;

	public void clickIssuesTab() throws InterruptedException {

		JavascriptExecutor executor = (JavascriptExecutor) driver;
		executor.executeScript("arguments[0].click();", issuesTab);
		//waitForElementVisibility(issuesTab);
		Thread.sleep(2000);
	}

	public void clearSearchField() throws InterruptedException {

		searchField.clear();
		searchField.sendKeys("sort:comments-desc");
		searchField.sendKeys(Keys.ENTER);
		// wait until the page is refreshed after pressing ENTER
		Thread.sleep(2000);
		//waitForElementStaleness(searchField);

	}

	public void filterByLabel() throws InterruptedException {

		labelSelectMenue.click();
		Thread.sleep(3000);
		//waitForElementVisibility(label); //wait until the labels are updated
		label.click();
		Thread.sleep(3000);
		//waitForElementStaleness(searchField); //wait until the labels is inserted to search filed
		 

	}

	public void pressOpenButton() throws InterruptedException {

		openButton.click();
		Thread.sleep(2000);

	}

	public String getTitle() throws InterruptedException {

		return title.getText();

	}

	public String getNrComments() throws InterruptedException {

		return comments.getText();

	}

	public String getIssueID() throws InterruptedException {

		String line = issueID.getText();
		// String value is: #9845 opened on Sep 19, 2021Sep 19, 2021 by gazal-k
		// extract the issue
		String[] splitIssueID = line.split(" ");
		String extractedIssueID = splitIssueID[0];
		return extractedIssueID;
		// String extractedUsername = splitIssueID[splitIssueID.length - 1];

	}

	public String getAuthor() throws InterruptedException {

		String line = issueID.getText();
		String[] splitIssueID = line.split(" ");
		String extractedUsername = splitIssueID[splitIssueID.length - 1];
		return extractedUsername;

	}

	public String getDate() throws InterruptedException {

		String datetimeValue = date.getAttribute("datetime");
		// Parse the datetime string
		LocalDateTime datetime = LocalDateTime.parse(datetimeValue, DateTimeFormatter.ISO_DATE_TIME);

		// Format the datetime as desired
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		String formattedDateTime = datetime.format(formatter);
		return formattedDateTime;

	}

	public List<WebElement> getLabels() throws InterruptedException {

		List<WebElement> labels = driver.findElements(By.cssSelector(
				"div[aria-label='Issues'] div[class*='js-navigation-container'] div[class*='Box-row ']:nth-child(1) div span a[class*='IssueLabel']"));
		return labels;

	}

	public void addLabelsToJson() throws InterruptedException {

		{
			for (int i = 0; i < getLabels().size(); i++) {
				WebElement element = getLabels().get(i);
				String labelText = element.getText();
				JsonCreator.createJsonObject("label" + (i + 1), labelText, "most-discussed-java-issue.json");
			}

		}

	}

}
