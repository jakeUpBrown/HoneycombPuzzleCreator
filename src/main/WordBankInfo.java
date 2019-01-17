package main;

import java.util.Comparator;
import java.util.List;

public class WordBankInfo {

    private String letters;
    private int mandatoryLetterIndex;
    private List<Word> allLetterWords;
    private List<Word> otherWordList;
    private int qualityValue;

    public WordBankInfo(WordBank bank, int mandatoryLetterIndex)
    {
        letters = bank.getLetters();
        allLetterWords = bank.getAllLetterWords();
        otherWordList = bank.getWordListFromIndex(mandatoryLetterIndex);
        this.mandatoryLetterIndex = mandatoryLetterIndex;
        qualityValue = bank.getQualityValue(mandatoryLetterIndex);

        // Make sure that the values are sorted by their difficulty rating, descending order
        Comparator wordSorter = new Comparator<Word>() {
            @Override
            public int compare(Word w1, Word w2) {
                // Compares w2 to w1, in order to have descending order
                return Double.valueOf(w2.getDifficultyRating()).compareTo(w1.getDifficultyRating());
            }
        };

        allLetterWords.sort(wordSorter);
        otherWordList.sort(wordSorter);
    }

    /**
     * get debug string
     * @return
     */
    public String toPrintString()
    {
        StringBuffer debugBuffer = new StringBuffer();

        debugBuffer.append("********************************************" + Constants.newLine);
        debugBuffer.append(Constants.newLine + Constants.newLine + "--- WORD BANK for letters ");
        debugBuffer.append(letters);
        debugBuffer.append(" - mandatory letter \'");
        debugBuffer.append(letters.charAt(mandatoryLetterIndex));
        debugBuffer.append("\'");
        debugBuffer.append(" --- (highest possible score = ");
        debugBuffer.append(getHighestPossibleScore(mandatoryLetterIndex));
        debugBuffer.append(")" + Constants.newLine);
        debugBuffer.append(Constants.newLine);
        debugBuffer.append("QUALITY VALUE : ");
        debugBuffer.append(qualityValue);
        debugBuffer.append(Constants.newLine);

        debugBuffer.append("--- ALL LETTER WORDS ---" + Constants.newLine);
        for(Word word : allLetterWords)
        {
            debugBuffer.append(word.getOutputString());
        }

        debugBuffer.append(Constants.newLine);
        debugBuffer.append(Constants.newLine);

        for(Word word : otherWordList)
        {
            debugBuffer.append(word.getOutputString());
        }
        debugBuffer.append("********************************************\n");

        return debugBuffer.toString();
    }

    /**
     * returns highest possible score depending on the scoring system defined in Constants
     * @param index
     * @return
     */
    private int getHighestPossibleScore(int index)
    {
        int highestScore = allLetterWords.size() * Constants.allLetterWordScore;
        highestScore += otherWordList.size() * Constants.regularWordScore;

        return highestScore;
    }
}
