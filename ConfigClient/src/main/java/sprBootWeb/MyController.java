package sprBootWeb;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class MyController {

	private static Logger logger = Logger.getLogger(MyController.class);
	
	@Autowired
	Book book;
	
    @RequestMapping("/")									// root mapping
    public String index(HttpServletRequest request) {
    	String rtnStr = " book.title="+book.getTitle()+"\n";
    	logger.info(rtnStr);
        return rtnStr;
    }

}
