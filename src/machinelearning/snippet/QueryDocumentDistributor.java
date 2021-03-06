package machinelearning.snippet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import search.object.Document;
import search.object.Query;
import search.object.Sentence;
import search.snippet.MysqlDriver;
import search.snippet.Record;

public class QueryDocumentDistributor {
	private int split = 1;
	private int number = 0;

	private String queryFile;
	private MysqlDriver driver = new MysqlDriver();

	public QueryDocumentDistributor(String queryFile) {
		this.queryFile = queryFile;
	}

	public QueryDocumentDistributor(String queryFile, int split, int number) {
		super();
		this.split = split;
		this.number = number;
		this.queryFile = queryFile;
	}

	public void distribute() throws Exception {
		driver.connect();
		Properties prop = new Properties();
		if (System.getProperty("config") != null) {
			String configFile = System.getProperty("config");
			System.out.println("Loading user specified config: " + configFile);
			prop.loadFromXML(new FileInputStream(configFile));
		}
		SnippetSVMLightInputGenerator gen = new SnippetSVMLightInputGenerator(
				prop);
		System.err.println();
		try {

			BufferedReader reader = new BufferedReader(
					new FileReader(queryFile));
			System.out.println(queryFile);
			String line = null;
			for (int i = 0; (line = reader.readLine()) != null; i++) {
				System.err.println("Processing " + i);
				if (i % split == number) {
					Query query = new Query(line);
					System.out.println(line);
					List<Record> records = driver.getRecord(query.getString(), true);
					for(int j = 0; j < records.size(); j++)
					{
						Record record = records.get(j);
						System.out.println(query.getString() + "\t" + record.getUrl());
						try
						{
							String pageContent = driver.getPage(record.getUrl());
							Document document = new Document(pageContent);
							Map<String, Double> sentenceScoreMap = driver
									.getTraining(query.getString(), record
											.getUrl());
							for (Sentence s : document.getSentences()) {
								if (sentenceScoreMap.containsKey(s.getString())) {
									try 
									{
										gen.addCase(s, query, sentenceScoreMap.get(s.getString()), i * 5 + j);
									} 
									catch (Exception e) 
									{
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						} 
						catch(Exception e)
						{
						}

						BufferedWriter writer = new BufferedWriter(
								new FileWriter("result-" + number));
						writer.write(gen.dumpToString());
						writer.close();
						// System.out.print(gen.dumpToString());
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {
		QueryDocumentDistributor distributor = null;
		System.out.println("starts");
		if (args.length != 2) {
			distributor = new QueryDocumentDistributor("query.data/query");
		} else {
			int split = Integer.parseInt(args[0]);
			int number = Integer.parseInt(args[1]);
			distributor = new QueryDocumentDistributor("query.data/query",
					split, number);
		}

		distributor.distribute();
	}
}
