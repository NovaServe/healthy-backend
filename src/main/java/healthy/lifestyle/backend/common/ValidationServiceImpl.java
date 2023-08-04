package healthy.lifestyle.backend.common;

import org.springframework.stereotype.Service;

@Service
public class ValidationServiceImpl implements ValidationService {
    @Override
    public boolean checkText(String input) {
        char[] notAllowed = new char[] {
            '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', '=', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', '0', ',', '<', '.', '>', '?', '\\', '`', '~'
        };
        for (char ch : notAllowed) {
            if (input.indexOf(ch) != -1) return false;
        }
        return true;
    }
}
