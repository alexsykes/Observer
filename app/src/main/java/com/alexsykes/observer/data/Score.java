package com.alexsykes.observer.data;

public class Score {
    int _id;
    final String sync;
    final String section;
    final String score;
    // String total;
    final String rider;
    final String lap;
    String trialid;
    String count;
    String observer;
    String created;
    String updated;
    String edited;
    String scoredata;

    public Score(String section, String score,  String rider, String lap, String trialid, String count, String observer, String created, String updated, String edited, String scoredata) {
        this.section = section;
        this.score = score;
        this.rider = rider;
        this.lap = lap;
        this.trialid = trialid;
        this.count = count;
        this.observer = observer;
        this.created = created;
        this.updated = updated;
        this.edited = edited;
        this.scoredata = scoredata;
        this.sync = "-1";
    }

    public Score(String section, String score,  String rider, String lap, String trialid, String count, String observer, String created, String updated, String edited, String scoredata, String sync) {
        this.section = section;
        this.score = score;
        this.rider = rider;
        this.lap = lap;
        this.trialid = trialid;
        this.count = count;
        this.observer = observer;
        this.created = created;
        this.updated = updated;
        this.edited = edited;
        this.scoredata = scoredata;
        this.sync = sync;
    }

    public Score(String section, String score, String rider, String lap, int _id, String observer) {
        this.section = section;
        this.score = score;
        this.rider = rider;
        this.lap = lap;
        this.sync = "-1";
        this._id = _id;
    }

    public Score(String section, String score, String rider, String lap, int _id, String observer, String sync) {
        this.section = section;
        this.score = score;
        this.rider = rider;
        this.lap = lap;
        this.sync = sync;
        this._id = _id;
    }

    public Score(String section, String score, String rider, String lap, int parseInt, String observer, String created, String sync) {
        this.section = section;
        this.score = score;
        this.rider = rider;
        this.lap = lap;
        this.sync = sync;
        this.created = created;
        this._id = _id;
    }

    public int get_id() {
        return _id;
    }

    public String getSection() {
        return section;
    }


    public String getRider() {
        return rider;
    }

    public String getTrialid() {
        return trialid;
    }

    public String getCount() {
        return count;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }

    public String getEdited() {
        return edited;
    }

    public String getScoredata() {
        return scoredata;
    }

    public String getObserver() {
        return observer;
    }

    public String getID(){
        return Integer.toString(_id);
    }

    public String getScore() {
        return score;
    }

    public String getLap() {
        return lap;
    }

    public String getSync() {
        return sync;
    }
}
