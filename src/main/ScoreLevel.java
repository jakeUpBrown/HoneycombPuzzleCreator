package main;

public class ScoreLevel {

    private String levelName;
    private int minLevelScore;
    private int preferredCountMin;
    private int preferredCountMax;

    // boolean that describes if it should be displayed on screen.
    private boolean displayed;

    public ScoreLevel(String levelName, int minLevelScore, int preferredCountMin, int preferredCountMax, boolean displayed)
    {
        this.levelName = levelName;
        this.minLevelScore = minLevelScore;
        this.preferredCountMin = preferredCountMin;
        this.preferredCountMax = preferredCountMax;
        this.displayed = displayed;
    }

    public int getMinLevelScore()
    {
        return minLevelScore;
    }

    public String getLevelName()
    {
        return levelName;
    }

    public int getPreferredCountMin()
    {
        return preferredCountMin;
    }

    public int getPreferredCountMax()
    {
        return preferredCountMax;
    }
}
