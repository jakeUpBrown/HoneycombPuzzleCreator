package main;

import java.util.*;

public class Constants {

    public final static String dictionaryName = "dictionary/ScrabbleDictionary.txt";
    public final static String createdDictionaryName = "DictionaryWithScores.txt";
    public final static String localCreatedDictionaryName;
    private final static String nGramFilePrefix = "dictionary/nGramInfo/1gram";
    public final static String totalSetOutputName = "Total_Set_Honeycomb";
    public final static String favorableSetOutputName = "Good_Set_Honeycomb";
    public final static int minStringSize = 5;
    public final static int totalLetters = 7;
    public final static int allLetterWordScore = 3;
    public final static int regularWordScore = 1;
    public final static int minBankScore = 25;
    public final static int nGramColCount;
    public final static String newLine = System.getProperty("line.separator");
    public static ScoreLevel minimumAllLetterLevel;
    public final static int valueForProperScoreLevels = 50;
    public final static int valueForBadPhrase = -30;
    public final static int valueForBadAllLetterWords = -40;

    // Don't include lowercase because it will converted to all uppercase before check
    public final static String nonAlphaRegex = "[^A-Z]+";

    public enum nGramCols
    {
        WORD,
        YEAR,
        MATCHCOUNT,
        VOLUMECOUNT;
    }

    public static String getNGramFileName(char c)
    {
        return nGramFilePrefix + Character.toUpperCase(c);
    }

    // Map of values of scrabble letters
    public final static Map<Character, Integer> lettersMap;

    // Map of score levels (good, excellent, genius, etc) and their associated word difficuly score
    public final static List<ScoreLevel> scoreLevels;

    static {
        // Add scrabble letter values into hashMap. Done this way to be readable.
        lettersMap = new HashMap<Character, Integer>();
        lettersMap.put('A',1);
        lettersMap.put('B',3);
        lettersMap.put('C',2);
        lettersMap.put('D',1);
        lettersMap.put('E',2);
        lettersMap.put('F',4);
        lettersMap.put('G',2);
        lettersMap.put('H',4);
        lettersMap.put('I',1);
        lettersMap.put('J',8);
        lettersMap.put('K',5);
        lettersMap.put('L',1);
        lettersMap.put('M',3);
        lettersMap.put('N',1);
        lettersMap.put('O',1);
        lettersMap.put('P',3);
        lettersMap.put('Q',10);
        lettersMap.put('R',1);
        lettersMap.put('S',1);
        lettersMap.put('T',1);
        lettersMap.put('U',1);
        lettersMap.put('V',4);
        lettersMap.put('W',4);
        lettersMap.put('X',8);
        lettersMap.put('Y',4);
        lettersMap.put('Z',10);

        // Add score level values
        scoreLevels = new ArrayList<ScoreLevel>();
        scoreLevels.add(new ScoreLevel("Good", 19, 5, 11, true));
        scoreLevels.add(new ScoreLevel("Excellent", 17, 11, 17, true));
        scoreLevels.add(new ScoreLevel("Genius", 15, 17, 25, true));
        scoreLevels.add(new ScoreLevel("Impossible", 0, 25, 60, false));

        // Make sure that the values are sorted by their minLevelScore, descending order
        scoreLevels.sort(new Comparator<ScoreLevel>() {
            public int compare(ScoreLevel sl1, ScoreLevel sl2)
            {
                if(sl1.getMinLevelScore() < sl2.getMinLevelScore())
                {
                    return 1;
                }
                else if(sl1.getMinLevelScore() == sl2.getMinLevelScore())
                {
                    return 0;
                }
                else
                {
                    return -1;
                }
            }
        });

        minimumAllLetterLevel = null;

        for(ScoreLevel level : scoreLevels)
        {
            if(level.getLevelName().equals("Genius"))
            {
                minimumAllLetterLevel = level;
                break;
            }
        }

        // set nGramColCount to avoid having to make this calculation many times
        nGramColCount = nGramCols.values().length;

        // name of created dictionary that's moved into dictionary package
        localCreatedDictionaryName = "dictionary/" + createdDictionaryName;
    }
}
