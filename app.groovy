@Grab('spring-boot-starter-thymeleaf')
@Grab('groovy-all')
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import groovy.json.*

@Controller
class IoTRestIfApp {
  private static final Logger logger = LoggerFactory.getLogger(IoTRestIfApp.class)

  @RequestMapping("/")
  String home() {
    "home"
  }

  @RequestMapping(value = "/tsv", method = RequestMethod.POST)
  @ResponseBody
  public String createTsv(@RequestBody String payload) {
    try{
      def jsonBody = new JsonSlurper().parseText(payload)
      def data = jsonBody["payload"]["data"]
      logger.info("post request: data=[${data}]")
      new File('C:/tmp/test.tsv').withWriterAppend('UTF-8') { w ->
        w << data.collect{ it.value }.join("\t") << "\n" // 改行文字はLFで固定
      }
    } catch(Exception e) {
      logger.error(e)
    }
    'OK'
  }
}
