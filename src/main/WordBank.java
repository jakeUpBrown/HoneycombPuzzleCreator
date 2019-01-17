package main;

import java.util.ArrayList;
import java.util.List;

public class WordBank
{
    private String letters;
    private List<Word> allLetterWords;

    private List<Word>[] mandatoryLetterBanks;

    private int[] qualityValues;

    public WordBank(String letters)
    {
        mandatoryLetterBanks = (ArrayList<Word>[]) new ArrayList[Constants.totalLetters];
        allLetterWords = new ArrayList<Word>();

        qualityValues = new int[Constants.totalLetters];

        this.letters = letters;
    }

    public WordBank(String letters, List<Word> allLetterWords, List<Word>[] mandatoryLetterBanks, int[] qualityValues)
    {
        this.letters = letters;
        this.allLetterWords = allLetterWords;
        this.mandatoryLetterBanks = mandatoryLetterBanks;
        this.qualityValues = qualityValues;
    }

    public void setLetters(String letters)
    {
        this.letters = letters;
    }

    public String getLetters()
    {
        return letters;
    }

    public void addAllLetterWord(Word newWord)
    {
        if (!allLetterWords.contains(newWord))
        {
            allLetterWords.add(newWord);
        }
    }

    public List<Word> getAllLetterWords()
    {
        return allLetterWords;
    }

    public void setMandatoryLetterBanks(List<Word>[] mandatoryLetterBanks)
    {
        this.mandatoryLetterBanks = mandatoryLetterBanks;
    }

    public List<Word> getWordListFromIndex(int index)
    {
        return mandatoryLetterBanks[index];
    }

    /**
     * generates and returns the object's information in a debug string.
     * @return
     */
    public String toPrintString()
    {
        StringBuffer debugBuffer = new StringBuffer();

        for(int i = 0; i < letters.length(); i++) {
            debugBuffer.append(debugStringFromIndex(i));
        }

        debugBuffer.append("\n");
        debugBuffer.append("\n");

        return debugBuffer.toString();
    }

    /**
     * get debug string from mandatory letter index given
     * @param index
     * @return
     */
    private StringBuffer debugStringFromIndex(int index)
    {
        StringBuffer debugBuffer = new StringBuffer();

        debugBuffer.append("********************************************" + Constants.newLine);
        debugBuffer.append(Constants.newLine + Constants.newLine + "--- WORD BANK for letters ");
        debugBuffer.append(letters);
        debugBuffer.append(" - mandatory letter \'");
        debugBuffer.append(letters.charAt(index));
        debugBuffer.append("\'");
        debugBuffer.append(" --- (highest possible score = ");
        debugBuffer.append(getHighestPossibleScore(index));
        debugBuffer.append(")" + Constants.newLine);

        debugBuffer.append("--- ALL LETTER WORDS ---" + Constants.newLine);
        for(Word word : allLetterWords)
        {
            debugBuffer.append(word.getOutputString());
        }

        debugBuffer.append(Constants.newLine);
        debugBuffer.append(Constants.newLine);

        for(Word word : mandatoryLetterBanks[index])
        {
            debugBuffer.append(word.getOutputString());
        }
        debugBuffer.append("********************************************\n");

        return debugBuffer;
    }

    public int getQualityValue(int index)
    {
        return qualityValues[index];
    }

    /**
     * returns highest possible score depending on the scoring system defined in Constants
     * @param index
     * @return
     */
    private int getHighestPossibleScore(int index)
    {
        int highestScore = allLetterWords.size() * Constants.allLetterWordScore;
        highestScore += mandatoryLetterBanks[index].size() * Constants.regularWordScore;

        return highestScore;
    }


    public void calculateAllValueScores()
    {
        for(int i = 0; i < qualityValues.length; i++)
        {
            qualityValues[i] = getValueFromCombination(i);
        }
    }

    /**
     * Get value from combination
     * @param index
     * @return
     */
    public int getValueFromCombination(int index)
    {

        char mandatoryLetter = letters.charAt(index);

        int value = 0;

        if(fallsWithinScoreRanges(index))
        {
            value += Constants.valueForProperScoreLevels;
        }

        value += getValueOfMandatoryLetter(mandatoryLetter);

        if(stringContainsBadPhrase(letters))
        {
            value += Constants.valueForBadPhrase;
        }

        if(!hasReasonableAllLetterWord())
        {
            value += Constants.valueForBadAllLetterWords;
        }

        return value;

    }


    /**
     * returns whether combination has an avoidable phrase
     * for now, just "ed", "s" and "ing" because it leads to repetitive puzzles
     * @param combination
     * @return
     */
    public boolean stringContainsBadPhrase(String combination)
    {
        boolean hasBad = false;

        // Check if has "ing"
        hasBad = combination.contains("I") && combination.contains("N") && combination.contains("G");

        // Check if string has "ed" (consecutive letters so search for DE string)
        hasBad |= combination.contains("DE");

        hasBad |= combination.contains("S");

        return hasBad;
    }

    /**
     * Returns value for how interesting the middle letter is.
     * Associated with scrabble letter score
     * @param letter
     * @return
     */
    private int getValueOfMandatoryLetter(char letter)
    {
        // Get scrabble letter score
        int value = Constants.lettersMap.get(letter);

        // Subtract 1 if vowel
        value -= isVowel(letter) ? 1 : 0;

        return value;
    }

    /**
     * returns whether the letter is a vowel or not
     * @param letter
     * @return
     */
    private boolean isVowel(char letter)
    {
        return letter == 'A' || letter == 'E' || letter == 'I' || letter == 'O' || letter == 'U';
    }

    /**
     * gets the info for a specific puzzle. (With a specified letter in the middle)
     * @param index
     * @return
     */
    public WordBankInfo getInfo(int index)
    {
        return new WordBankInfo(this, index);
    }

    public boolean fallsWithinScoreRanges(int index)
    {
        int[][] scoresPerLevel = findScorePerScoreLevel();

        int i = 0;

        for(ScoreLevel level : Constants.scoreLevels)
        {
            int totalScoreAbove = 0;

            for(int j = 0; j <= i; j++)
            {
                totalScoreAbove += scoresPerLevel[index][j];
            }

            if(totalScoreAbove < level.getPreferredCountMin() || totalScoreAbove > level.getPreferredCountMax())
            {
                return false;
            }

            i++;
        }

        return true;
    }

    public boolean hasReasonableAllLetterWord()
    {
        if(Constants.minimumAllLetterLevel == null)
        {
            return true;
        }

        for(Word allLetterWord : allLetterWords)
        {
            if(allLetterWord.getDifficultyRating() > Constants.minimumAllLetterLevel.getMinLevelScore())
            {
                return true;
            }
        }

        return false;
    }


    public int[][] findScorePerScoreLevel()
    {
        // return value. first index is for each mandatory letter, second index is for each score level
        int[][] scorePerScoreLevel = new int[Constants.totalLetters][Constants.scoreLevels.size()];

        for(Word word : allLetterWords)
        {
            int indexToAdd = 0;

            for(int i = 0; i < Constants.scoreLevels.size() - 1; i++)
            {
                if(word.getDifficultyRating() >= Constants.scoreLevels.get(i).getMinLevelScore())
                {
                    indexToAdd = i;
                    break;
                }

                indexToAdd = i;
            }

            // add to all mandatory letter fields
            for(int i = 0; i < scorePerScoreLevel.length; i++)
            {
                scorePerScoreLevel[i][indexToAdd] += Constants.allLetterWordScore;
            }

        }

        for(int i = 0; i < scorePerScoreLevel.length; i++)
        {
            for(Word word : mandatoryLetterBanks[i]) {

                int j;

                for(j = 0; j < Constants.scoreLevels.size() - 1; j++)
                {
                    if(word.getDifficultyRating() >= Constants.scoreLevels.get(j).getMinLevelScore())
                    {
                        break;
                    }
                }

                scorePerScoreLevel[i][j]++;
            }

        }

        return scorePerScoreLevel;
    }

}
