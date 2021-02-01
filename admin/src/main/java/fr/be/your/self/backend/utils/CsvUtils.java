package fr.be.your.self.backend.utils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.MappingStrategy;

import fr.be.your.self.model.Subscription;

public enum CsvUtils {
	SINGLETON;
	
	public <T> List<T> readCsvFile(MultipartFile file, MappingStrategy<T> strategy, Class clazz) throws Exception {
		Reader reader = new InputStreamReader(file.getInputStream());
		CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(0).build();

		CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(csvReader).withType(clazz)
										.withMappingStrategy(strategy).build();
		List<T> results = csvToBean.parse();
		reader.close();
		return results;
	}
	

	public Subscription createSubscriptionFromCsv(SubscriptionCsv subCsv) throws ParseException {
		Subscription sub = new Subscription();
		sub.setCanal(subCsv.getCanal());
		sub.setDuration(subCsv.getDuration());
		sub.setPaymentGateway(subCsv.getPaymenGateway());
		sub.setPrice(subCsv.getPrice());
		sub.setSubscriptionStartDate(getDateFromStr(subCsv.getStartDate()));
		sub.setSubscriptionEndDate(getDateFromStr(subCsv.getEndDate()));
		sub.setValidStartDate(getDateFromStr(subCsv.getStartDate()));
		sub.setValidEndDate(getDateFromStr(subCsv.getEndDate()));
		sub.setTerminationAsked(subCsv.isTerminationAsked());
		sub.setStatus(true);
		return sub;
	}
	
	public static List<SubscriptionCsv> extractSubscriptionCsv(Iterable<Subscription> subscriptions) {
		List<SubscriptionCsv> returnList = new ArrayList<SubscriptionCsv>();
		for (Subscription subscription : subscriptions) {
			SubscriptionCsv subscriptionCsv = new SubscriptionCsv(subscription);
			returnList.add(subscriptionCsv);
		}
		
		return returnList;
	}

	private java.util.Date getDateFromStr(String dateStr) throws ParseException {
		String pattern = SubscriptionCsv.DATE_FORMAT;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		java.util.Date date = new java.util.Date(simpleDateFormat.parse(dateStr).getTime());
		return date;
	}
}
