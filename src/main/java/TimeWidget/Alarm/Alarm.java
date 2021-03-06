package TimeWidget.Alarm;

import TimeWidget.Container.TimeWidget;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static TimeWidget.Index.addExecutors;
import static TimeWidget.Index.removeExecutors;

public class Alarm extends TimeWidget {

    private AlarmView alarmView;
    private LocalTime time;
    private long snoozetime;
    private BorderPane alarmswitchPane;
    private ImageView offalarmswitch, onalarmswitch, snoozeswitch;
    private String timeformat;
    final private Alarm alarm = this;


    public Alarm(AlarmView alarmView, Stage owner, String name, LocalTime time, String format, int snoozetime, String media) {
        this.alarmView = alarmView;
        this.owner = owner;
        this.name = name;
        this.time = time;
        this.timeformat = format;
        this.snoozetime = snoozetime;
        this.mediasrc = media;
        createWidget();
    }

    @Override
    protected void createWidgetBottom() {
        //widget.setGridLinesVisible(true);

        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setPercentHeight(33);
        widget.getRowConstraints().addAll(rowConstraints);

        titlepane.setOnMouseClicked(event -> {
            new AlarmUpdate(owner, alarmView, alarm);
        });

        BorderPane borderPane = new BorderPane();
        timetxt = new Text(formatTime(this.time));
        borderPane.setCenter(timetxt);
        borderPane.setOnMouseClicked(event -> {
            new AlarmUpdate(owner, alarmView, alarm);
        });
        widget.add(borderPane, 0, 1, 3, 1);


        alarmswitchPane = new BorderPane();
        offalarmswitch = new ImageView(new Image(getClass().getResourceAsStream("/ic_alarm_black_48dp_1x.png")));
        onalarmswitch = new ImageView(new Image(getClass().getResourceAsStream("/ic_alarm_white_24dp_2x.png")));
        snoozeswitch = new ImageView(new Image(getClass().getResourceAsStream("/ic_snooze_white_24dp_2x.png")));
        alarmswitchPane.setCenter(onalarmswitch);
        widget.add(alarmswitchPane, 3,1);
        alarmswitchPane.setOnMouseClicked(event -> {
            if (alarmswitchPane.getChildren().contains(onalarmswitch) || alarmswitchPane.getChildren().contains(snoozeswitch)) {
                alarmswitchPane.setCenter(offalarmswitch);
                cancelExecutor();
            }
            else {
                alarmswitchPane.setCenter(onalarmswitch);
                futureTask = createFutureTask();
            }
        });

        executeExecutor();
    }

    public void updateAlarm(String name, LocalTime time, String format, int snoozetime, String media) {
        this.name = name;
        this.time = time;
        this.timeformat = format;
        this.snoozetime = snoozetime;
        this.mediasrc = media;
        titletxt.setText(name);
        timetxt.setText(formatTime(time));
        futureTask.cancel(true);
        futureTask = createFutureTask();
    }

    @Override
    protected void executeExecutor() {
        executor = new ScheduledThreadPoolExecutor(2);
        addExecutors(executor);
        futureTask = createFutureTask();
    }

    @Override
    protected void resetTime() {

    }

    @Override
    protected ScheduledFuture<?> createFutureTask() {
        LocalTime now = LocalTime.now();
        Duration delayduration = Duration.between(now, time).isNegative() ? Duration.between(now, time).plusHours(24) :Duration.between(now, time);
        ScheduledFuture scheduledFuture = executor.schedule(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        new AlarmNotify(alarm, owner, name, time, timeformat, mediasrc, snoozetime);
                    }
                });
                if(futureTask.isDone()) {
                    futureTask = createFutureTask();
                }
            }
        },delayduration.toMillis(), TimeUnit.MILLISECONDS);


        return scheduledFuture;
    }

    protected String formatTime(LocalTime time){
        String timetxt;
        if (timeformat.equals("24")){
            timetxt = time.toString();
        }else {
            timetxt = time.format(DateTimeFormatter.ofPattern("hh:mm a"));
        }
        return timetxt;
    }

    @Override
    protected void updateGUI() {

    }

    @Override
    protected void closeEvent() {
        cancelExecutor();
        getExecutor().shutdown();
        removeExecutors(executor);
        alarmView.getAlarms().remove(getWidget());
    }

    public void setSnoozeswitch(){
        alarmswitchPane.setCenter(snoozeswitch);
    }

    public String getTimeformat() {
        return this.timeformat;
    }

    public long getSnoozetime() {
        return this.snoozetime;
    }

    public LocalTime getTime() {
        return this.time;
    }

    public String getMediasrc() {
        return this.mediasrc;
    }
}
