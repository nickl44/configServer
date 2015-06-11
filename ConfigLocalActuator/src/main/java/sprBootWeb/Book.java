package sprBootWeb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Book {

	@Value("${title}")
	private String title;

	public String getTitle() {
		return title;
	}
}
