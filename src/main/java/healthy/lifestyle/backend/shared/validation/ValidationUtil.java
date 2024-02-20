package healthy.lifestyle.backend.shared.validation;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ValidationUtil {
    private final char[] alphabet = new char[] {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    private final char[] space = new char[] {' '};

    private final char[] digits = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private final char[] allowedTextSpecialSymbols =
            new char[] {'.', ',', '-', ':', ';', '!', '?', '\'', '\"', '#', '%', '(', ')', '+', '='};

    private final char[] allowedTitleSpecialSymbols = new char[] {'.', ',', '-', '(', ')', '/'};

    public boolean isLetter(char c) {
        boolean isLetter = false;
        for (char letter : this.alphabet) {
            if (c == letter) {
                isLetter = true;
                break;
            }
        }
        return isLetter;
    }

    public boolean isDigit(char c) {
        boolean isNumber = false;
        for (int number : this.digits) {
            if (c == number) {
                isNumber = true;
                break;
            }
        }
        return isNumber;
    }

    public boolean isSpace(char c) {
        return c == this.space[0];
    }

    private boolean isAllowedTextSpecialSymbol(char c) {
        boolean isAllowedSpecialSymbol = false;
        for (char i : this.allowedTextSpecialSymbols) {
            if (c == i) {
                isAllowedSpecialSymbol = true;
                break;
            }
        }
        return isAllowedSpecialSymbol;
    }

    private boolean isAllowedTitleSpecialSymbol(char c) {
        boolean isAllowedSpecialSymbol = false;
        for (char i : this.allowedTitleSpecialSymbols) {
            if (c == i) {
                isAllowedSpecialSymbol = true;
                break;
            }
        }
        return isAllowedSpecialSymbol;
    }

    public boolean isValidTitle(String title) {
        String trim = title.trim();
        char[] charArr = trim.toCharArray();
        boolean isValid = true;
        for (char c : charArr) {
            if (!(this.isLetter(c) || this.isDigit(c) || this.isSpace(c) || this.isAllowedTitleSpecialSymbol(c))) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    public boolean isValidDescription(String description) {
        String trim = description.trim();
        char[] charArr = trim.toCharArray();
        boolean isValid = true;
        for (char c : charArr) {
            if (!(this.isLetter(c) || this.isDigit(c) || this.isSpace(c) || this.isAllowedTextSpecialSymbol(c))) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    public boolean isValidWebLink(String webLink) {
        String trim = webLink.trim();
        List<Integer> dotIndexes = new ArrayList<>();
        char[] webLinkTrimArr = trim.toCharArray();
        for (int i = 0; i < webLinkTrimArr.length; i++) {
            if (webLinkTrimArr[i] == '.') {
                dotIndexes.add(i);
            }
        }
        if (dotIndexes.size() != 1) return false;
        return trim.startsWith("http://") || trim.startsWith("https://");
    }

    public boolean isValidId(Long id) {
        if (id == null) return false;
        return id >= 0;
    }

    public boolean isNotEmptyList(List<?> list) {
        return !list.isEmpty();
    }
}
