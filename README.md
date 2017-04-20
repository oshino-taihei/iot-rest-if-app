# iot-rest-if-app
Oracle IoT Cloud ServiceからのREST外部連携メッセージを受信し、ファイル出力やデータベース連携を行う。

## 前提
libフォルダにodbc7.jarを配置しmavenのローカルリポジトリにインストールする。
~~~~
mvn install:install-file -Dfile=lib/ojdbc7.jar -DgroupId=com.oracle -DartifactId=ojdbc7 -Dversion=12.1.0 -Dpackaging=jar
~~~~
