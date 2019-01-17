package main;

import java.text.DecimalFormat;

public class Word {

    private String word;
    private long countFrequency;
    private long volumeFrequency;

    public Word(String word)
    {
        this.word = word;
        countFrequency = 0;
        volumeFrequency = 0;
    }

    public Word(String word, long countFrequency, long volumeFrequency)
    {
        this.word = word;
        this.countFrequency = countFrequency;
        this.volumeFrequency = volumeFrequency;
    }

    public String getWord()
    {
        return word;
    }

    public void addToCountFrequency(long add)
    {
        countFrequency += add;
    }

    public void addToVolumeFrequency(long add)
    {
        volumeFrequency += add;
    }

    public long getCountFrequency()
    {
        return countFrequency;
    }

    public long getVolumeFrequency()
    {
        return volumeFrequency;
    }

    /**
     * Returns a difficulty value based on the difficulty equation
     * For now, that equation is Log(base 2) of countFrequency / [number of letters in word]
     * @return
     */
    public double getDifficultyRating()
    {
        long diffPerLetter = countFrequency / word.length();

        if(diffPerLetter < 1)
        {
            diffPerLetter = 1;
        }

        return Math.log(diffPerLetter) / Math.log(2);
    }

    public String getDictionaryWithScoresString()
    {

        StringBuffer buffer = new StringBuffer(word);
        buffer.append("\t");
        buffer.append(countFrequency);
        buffer.append("\t");
        buffer.append(volumeFrequency);

        return buffer.toString();
    }

    public String getOutputString()
    {
        DecimalFormat df = new DecimalFormat("0.00");

        StringBuffer buffer = new StringBuffer(word);
        buffer.append("\t");
        buffer.append(df.format(getDifficultyRating()));
        buffer.append(Constants.newLine);

        return buffer.toString();
    }
}
