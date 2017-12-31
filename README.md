# Remind Me #

Remind Me is a simple app to text you reminders on a periodic schedule.


### Usage

Reminders can be useful for recurring tasks ("take out the trash") or as daily reminders until the task has been completed ("file my taxes")

```
Usage:

 1) To subscribe to reminders:

   => Text: "<Frequency> <Message>"

   <daily/weekly/weekdays/weekends/monday/tuesday/wednesday/thursday/friday/saturday/sunday> <message>

   Ex.
     MONDAY take out the trash
     DAILY file my taxes
     WEEKENDS walk the dog


 2) To stop receiving reminders:

   => Text : "COMPLETED <Reminder ID>"

   Ex.
     COMPLETED        # stop receiving all reminders to your account
     COMPLETED 1234   # stop receiving reminder ID 1234 to your number
```


## Build & Run ##

```sh
$ cd Remind_Me
$ ./sbt
> jetty:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.

Configure your Twilio account credentials under `src/main/resources/twilio_account.conf` and set up a Twilio callback URL for the number configured in twilio_account.conf under `phonenumber` by navigating to `https://www.twilio.com/console/phone-numbers/{PhoneNumberSID}` and under Messaging setting `A Message Comes In ` to send a POST to /reminders endpoint at your public server address. Configure with NGROK to run on a local machine.


### TODO

* Spreadsheet modification is not threadsafe
* Replace spreadsheet with Google Sheets API or spin up a Database
* Tests