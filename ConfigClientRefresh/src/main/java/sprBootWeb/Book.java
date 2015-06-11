package sprBootWeb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class Book {

	@Value("${title}")
	private String title;

	public String getTitle() {
		return title;
	}
}
