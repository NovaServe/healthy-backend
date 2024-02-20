package healthy.lifestyle.backend.user.validation;

import healthy.lifestyle.backend.shared.validation.ValidationUtil;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserValidationUtil {
    @Autowired
    ValidationUtil validationUtil;

    private final char[] allowedUsernameSpecialSymbols = new char[] {'.', '-', '_'};

    private final char[] allowedEmailSpecialSymbolsFirstPart = new char[] {'.', '-', '_'};

    private final char[] allowedEmailSpecialSymbolsSecondPart = new char[] {'-', '_'};

    private final char[] allowedPasswordSpecialSymbols = new char[] {
        '.', ',', '-', '_', '<', '>', ':', ';', '!', '?', '#', '$', '%', '^', '&', '*', '(', ')', '+', '='
    };

    private boolean isAllowedUsernameSpecialSymbol(char c) {
        boolean isAllowedSpecialSymbol = false;
        for (char i : this.allowedUsernameSpecialSymbols) {
            if (c == i) {
                isAllowedSpecialSymbol = true;
                break;
            }
        }
        return isAllowedSpecialSymbol;
    }

    private boolean isAllowedEmailSpecialSymbolFirstPart(char c) {
        boolean isAllowedSpecialSymbol = false;
        for (char i : this.allowedEmailSpecialSymbolsFirstPart) {
            if (c == i) {
                isAllowedSpecialSymbol = true;
                break;
            }
        }
        return isAllowedSpecialSymbol;
    }

    private boolean isAllowedEmailSpecialSymbolSecondPart(char c) {
        boolean isAllowedSpecialSymbol = false;
        for (char i : this.allowedEmailSpecialSymbolsSecondPart) {
            if (c == i) {
                isAllowedSpecialSymbol = true;
                break;
            }
        }
        return isAllowedSpecialSymbol;
    }

    private boolean isAllowedPasswordSpecialSymbol(char c) {
        boolean isAllowedSpecialSymbol = false;
        for (char i : this.allowedPasswordSpecialSymbols) {
            if (c == i) {
                isAllowedSpecialSymbol = true;
                break;
            }
        }
        return isAllowedSpecialSymbol;
    }

    public boolean isValidEmail(String email) {
        // <firstPart>@<secondPart>.<thirdPart>
        // if (email == null || email.isBlank() || email.length() < 7 || email.length() > 64) return false;
        String trimEmail = email.trim();

        List<Integer> atIndexes = new ArrayList<>();
        char[] trimArr = trimEmail.toCharArray();
        for (int i = 0; i < trimArr.length; i++) {
            if (trimArr[i] == '@') {
                atIndexes.add(i);
            }
        }
        if (atIndexes.size() != 1) return false;

        String[] trimEmailSplit = trimEmail.split("@");
        char[] firstPartArr = trimEmailSplit[0].toCharArray();
        if (firstPartArr.length < 1) return false;
        for (char c : firstPartArr) {
            if (!(isAllowedEmailSpecialSymbolFirstPart(c) || validationUtil.isLetter(c) || validationUtil.isDigit(c))) {
                return false;
            }
        }

        String secondAndThirdParts = trimEmailSplit[1];
        char[] secondAndThirdPartsArr = secondAndThirdParts.toCharArray();
        List<Integer> dotIndexes = new ArrayList<>();
        for (int i = 0; i < secondAndThirdPartsArr.length; i++) {
            if (secondAndThirdPartsArr[i] == '.') {
                dotIndexes.add(i);
            }
        }
        if (dotIndexes.size() != 1) return false;

        String[] secondAndThirdPartsSplit = secondAndThirdParts.split("\\.");
        char[] secondPartArr = secondAndThirdPartsSplit[0].toCharArray();
        char[] thirdPartArr = secondAndThirdPartsSplit[1].toCharArray();
        if (secondPartArr.length < 2 || thirdPartArr.length < 2) return false;

        for (char c : secondPartArr) {
            if (!(isAllowedEmailSpecialSymbolSecondPart(c)
                    || validationUtil.isLetter(c)
                    || validationUtil.isDigit(c))) {
                return false;
            }
        }

        for (char c : thirdPartArr) {
            if (!validationUtil.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    public boolean isValidUsername(String username) {
        // if (username == null || username.isBlank() || username.length() < 6 || username.length() > 20) return false;
        String trim = username.trim();
        char[] charArr = trim.toCharArray();
        boolean isValid = true;
        for (char c : charArr) {
            if (!(this.validationUtil.isLetter(c)
                    || this.validationUtil.isDigit(c)
                    || this.isAllowedUsernameSpecialSymbol(c))) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    public boolean isValidFullName(String fullName) {
        // if (fullName == null || fullName.isBlank() || fullName.length() < 2 || fullName.length() > 64) return false;
        String trim = fullName.trim();
        char[] charArr = trim.toCharArray();
        boolean isValid = true;
        for (char c : charArr) {
            if (!(validationUtil.isLetter(c) || validationUtil.isSpace(c))) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    public boolean isValidPassword(String password) {
        // if (password == null || password.isBlank() || password.length() < 10 || password.length() > 64) return false;
        String trim = password.trim();
        char[] charArr = trim.toCharArray();
        boolean isValid = true;
        for (char c : charArr) {
            if (!(validationUtil.isLetter(c) || validationUtil.isDigit(c) || this.isAllowedPasswordSpecialSymbol(c))) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    public boolean isValidAge(int age) {
        return age >= 16 && age <= 120;
    }
}
