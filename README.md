<h3>CCU Batch Purge by File</h3>
<hr>
<p>
This is a CLI to batch purge by URL. The batch is in the form of file (or files) containing list of URLs (one URL per line)
or raw CCU-JSON where the URLs listed inside "objects" JSONArray
</p>

<h5>Prerequisite</h5>
<ul>
    <li>Java installed in the local (note: I use JAVA 11 in this sample, but if you have different JAVA version locally, change the "properties" tag in pom.xml</li>
    <li>Maven installed in local</li>
</ul>
<br>
<h5>Setup</h5>
<ul>
    <li>Clone this project</li>
    <li>Go to that root dir of the project</li>
    <li>Run: mvn clean install</li>
    <li>It will produce file ccudeleteurlbyfiles.jar inside "target" folder</li>
</ul>
<br>
<h5>How to use the CLI</h5>
<p>CCU CLI takes 5 arguments separated by single space. These arguments are:</p>
<ul>
   <li>
   args[0] is location of .edgerc file. This file contain Akamai API client credentials (client token,
   access token, secret, host, and max body size) which necessary for EdgeGrid lib
   sample:
   host = https://akab-xxxxx.luna.akamaiapis.net
   client_token = akab-xxxxx
   client_secret = xxxxx
   access_token = xxxx
   </li>
   <li>args[1] is type of purge - options are: 'delete' or 'invalidate'</li>
   <li>args[2] is target network - options are: 'staging' or 'production'</li>
   <li>args[3] is  full path to the directory which has file(s) containing either list of URLs or raw CCU JSON to be executed synchronouslyargs[4] is number of seconds the CLI will pause between each CCU API call execution</li>
   <li>
   args[4] is number of seconds the CLI will pause between each CCU API call execution
   </li>
</ul>
<br>
<h5>Sample Usage</h5>
<ol>
    <li> Delete all URLs listed inside all files within /home/user/fileToPurge directory in Staging network<br>
    java -jar ccu.jar /home/user/token.txt delete staging /home/user/fileToPurge 5</li>
    <li>Invalidate all URLs listed inside all files within /home/user/fileToPurge directory in production network<br>
    java -jar ccu.jar /home/user/token.txt invalidate production /home/user/fileToPurge 10</li>   
</ol>


