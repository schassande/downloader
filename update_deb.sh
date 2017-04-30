echo "Stop existing service"
sudo service downloader stop
echo "Create package"
./mvnw -Dmaven.test.skip=true package
echo "Update package content"
sudo cp target/downloader-0.0.1-SNAPSHOT.war /usr/share/downloader/lib/
sudo systemctl daemon-reload
echo "Restart service"
sudo service downloader start
tail -f  /var/log/downloader/logFile.`date +%Y-%m-%d`.log

