package homework;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.json.simple.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import homework.SeleniumPage;

public class Exercise4 {

	public static void main(String[] args) throws InterruptedException {
		WebDriverManager.chromedriver().setup();
		WebDriver driver = new ChromeDriver();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		driver.manage().window().maximize();
		driver.get("https://github.com/SeleniumHQ/selenium");
		SeleniumPage landingPage = new SeleniumPage(driver);

		// 1.a Open the dialog “Code”
		landingPage.openDialogCode();

		// 1.b Get the repository clone URL from the input field
		String url = landingPage.getRepoURL();

		// 1.c Get the number of releases
		String releases = landingPage.getNrReleases();

		// 1.d Create a JSON object and store the (url, releases);
		JsonCreator.createJsonObject("url", url, "selenium-meta-data.json");
		JsonCreator.createJsonObject("releases", releases, "selenium-meta-data.json");

		////// Part2 //////
		IssuesPage issuesPage = new IssuesPage(driver);

		// Click on tab “Issues”
		issuesPage.clickIssuesTab();

		// 2.a Clear search field and type “sort:comments-desc”
		issuesPage.clearSearchField();

		// 2.b Filter by label “C-java”
		issuesPage.filterByLabel();

		// 2.c press Open button
		issuesPage.pressOpenButton();

		// 2.d Get the following attributes of the first (most top) issue displayed

		String title = issuesPage.getTitle();

		String nr_comments = issuesPage.getNrComments();

		String issueID = issuesPage.getIssueID();

		String author = issuesPage.getAuthor();

		String date = issuesPage.getDate();

		// 2.d Write the information to a JSON file

		JsonCreator.createJsonObject("title", title, "most-discussed-java-issue.json");
		issuesPage.addLabelsToJson();
		JsonCreator.createJsonObject("number of comments", nr_comments, "most-discussed-java-issue.json");
		JsonCreator.createJsonObject("issueID", issueID, "most-discussed-java-issue.json");
		JsonCreator.createJsonObject("author", author, "most-discussed-java-issue.json");
		JsonCreator.createJsonObject("Date", date, "most-discussed-java-issue.json");

		Thread.sleep(2000);
		driver.quit();
	}

}
