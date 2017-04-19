@Grab('groovy-all')
import org.slf4j.*
import groovy.json.*

@Controller
class IoTRestIfApp {
  private static final Logger logger = LoggerFactory.getLogger(IoTRestIfApp.class)
  private static final String IF_DIR = '/u01/shares/IF'

  @RequestMapping("/")
  String home() { 'please POST request to /toApriso/<IFID> or /toEBS/<IFID>' }

  @RequestMapping(value = "/toApriso/{ifid}", method = RequestMethod.POST)
  @ResponseBody
  public String createAprisoTsv(@PathVariable String ifid, @RequestBody String payload) {
    createTsv(new File("${IF_DIR}/toApriso/${ifid}.tsv"), payload)
  }

  @RequestMapping(value = "/toEBS/{ifid}", method = RequestMethod.POST)
  @ResponseBody
  public String createEbsTsv(@PathVariable String ifid, @RequestBody String payload) {
    createTsv(new File("${IF_DIR}/toEBS/${ifid}.tsv"), payload)
  }

  private String createTsv(File tsvFile, String payload) {
    try{
      def json = new JsonSlurper().parseText(payload)
      def message = json["payload"]["data"].collect{ it.value }.join("\t")
      logger.info("file=[${tsvFile}] message=[${message}]")
      tsvFile.withWriterAppend('UTF-8') { writer -> writer << message +  "\n" }
    } catch(e) {
      logger.error(e.getMessage())
      return "{\"status\":\"FAILURE\",\"error\":\"${e.getMessage()}\"}"
    }
    '{"status":"SUCCESS"}'
  }
}
