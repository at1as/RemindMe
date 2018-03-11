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

* First, configure your Twilio account credentials under `src/main/resources/twilio_account.conf`
* Next, configure a Twilio callback URL for the number specified in twilio_account.conf under `phonenumber` by navigating to `https://www.twilio.com/console/phone-numbers/{PhoneNumberSID}` and under Messaging setting `A Message Comes In ` to send a POST to /reminders endpoint at your public server address. Suggest using NGROK to connect the callback URL to your local machine over a public URL

```sh
$ cd Remind_Me
$ sbt
> jetty:start
```
And optionally, to use ngrok
```
$ ngrok http 8080
> Forwarding http://206934cd.ngrok.io -> localhost:8080
```

In the above example, `http://206934cd.ngrok.io/reminders` becomes the Twilio callback URL

To send out the reminders, POST to the `/scheduler` endpoint. As reminders don't currently accept a time, only a day, it is recommended to have a cron hit this once, on a daily interval.


### TODO

* Replace spreadsheet with Google Sheets API or spin up a Database
* Accept time/time-ranges for reminders
* Tests