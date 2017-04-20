@Grab('groovy-all')
@Grab(group='com.oracle', module='ojdbc7', version='12.1.0')
import java.time.*
import java.time.format.*
import java.sql.*
import groovy.json.*
import org.slf4j.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

@Controller
class IoTRestIfApp {
  private static final Logger logger = LoggerFactory.getLogger(IoTRestIfApp.class)
  private static final String IF_DIR = '/u01/shares/IF'
  private static final String GET_RESPONSE = 'GET is not supported. Please use POST requests.'

  @Autowired
  private JdbcTemplate jdbc

  @RequestMapping("/")
  @ResponseBody
  String home() { 'please POST request to /Apriso/<IFID> or /EBS/<IFID> or /DB' }

  @RequestMapping(value = "/Apriso/{ifid}", method = RequestMethod.GET)
  @ResponseBody
  public String getAprisoTsv() { GET_RESPONSE }

  @RequestMapping(value = "/Apriso/{ifid}", method = RequestMethod.POST)
  @ResponseBody
  public String createAprisoTsv(@PathVariable String ifid, @RequestBody String payload) {
    createTsv(new File("${IF_DIR}/toApriso/${ifid}.tsv"), payload)
  }

  @RequestMapping(value = "/EBS/{ifid}", method = RequestMethod.GET)
  @ResponseBody
  public String getEbsTsv() { GET_RESPONSE }

  @RequestMapping(value = "/EBS/{ifid}", method = RequestMethod.POST)
  @ResponseBody
  public String createEbsTsv(@PathVariable String ifid, @RequestBody String payload) {
    createTsv(new File("${IF_DIR}/toEBS/${ifid}.tsv"), payload)
  }

  @RequestMapping(value = '/DB' , method = RequestMethod.GET)
  @ResponseBody
  public String getDb() { GET_RESPONSE }

  @RequestMapping(value = '/DB' , method = RequestMethod.POST)
  @ResponseBody
  public String createData(@RequestBody String payload) {
    try {
      def json = new JsonSlurper().parseText(payload)
      def deviceId = json[0]["clientId"]
      def creationDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(json[0]["eventTime"] as Long), ZoneId.of("Asia/Tokyo"));
      def format = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.JAPAN)
      def data = JsonOutput.toJson(json[0]["payload"]["data"])
      def sql = "insert into XXIOT_DATA_DETAILS(device_id, creation_date, data) values ('${deviceId}', to_date('${creationDateTime.format(format)}', 'YYYY/MM/DD HH24:MI:SS'), '${data}')"
      logger.info("SQL=[${sql}]")
      jdbc.update(sql) //TODO:バインド変数化
    } catch(e) {
      logger.error("error=[${e.getMessage()} payload=[${payload}]")
      return "{\"status\":\"FAILURE\",\"error\":\"${e.getMessage()}\"}"
    }
    '{"status":"SUCCESS"}'
  }

  private String createTsv(File tsvFile, String payload) {
    try{
      def json = new JsonSlurper().parseText(payload)
      def message = json[0]["payload"]["data"].collect{ it.value }.join("\t")
      logger.info("file=[${tsvFile}] message=[${message}]")
      tsvFile.withWriterAppend('UTF-8') { writer -> writer << message +  "\n" }
    } catch(e) {
      logger.error(e.getMessage())
      return "{\"status\":\"FAILURE\",\"error\":\"${e.getMessage()}\"}"
    }
    '{"status":"SUCCESS"}'
  }
}
