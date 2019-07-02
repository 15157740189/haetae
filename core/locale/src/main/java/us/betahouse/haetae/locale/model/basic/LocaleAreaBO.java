package us.betahouse.haetae.locale.model.basic;

import us.betahouse.util.common.ToString;

public class LocaleAreaBO extends ToString {
    private static final long serialVersionUID = 3114186423378005529L;
    /**
     * 占用ID
     */
    private String localeAreaId;
    /**
     * 场地ID
     */

    private String localeId;
    /**
     * 场地借用者
     */

    private String userId;
    /**
     * 场地使用日期
     */

    private String timeDate;
    /**
     * 场地使用时间段
     */

    private String timeBucket;
    /**
     * 是否状况 三种：提交申请 申请中 取消
     */

    private String status;

    public String getLocaleAreaId() {
        return localeAreaId;
    }

    public void setLocaleAreaId(String localeAreaId) {
        this.localeAreaId = localeAreaId;
    }

    public String getLocaleId() {
        return localeId;
    }

    public void setLocaleId(String localeId) {
        this.localeId = localeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTimeDate() {
        return timeDate;
    }

    public void setTimeDate(String timeDate) {
        this.timeDate = timeDate;
    }

    public String getTimeBucket() {
        return timeBucket;
    }

    public void setTimeBucket(String timeBucket) {
        this.timeBucket = timeBucket;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
