package io.github.jumperonjava.kpz_atm_mod.client.ui.state;

import io.github.jumperonjava.kpz_atm_mod.client.SimpleRequestQueue;
import io.github.jumperonjava.kpz_atm_mod.client.ui.AtmScreen;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.Button;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.TextComponent;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.TextInput;
import io.github.jumperonjava.kpz_atm_mod.server.Status;
import net.minecraft.text.Text;

import java.util.Map;

public class RegisterState extends GenericState {
    private TextComponent warningText;

    public RegisterState(AtmScreen parent) {
        super(parent);
    }

    @Override
    public Text title() {
        return Text.translatable("register.title");
    }

    public void setUsername(String username) {
        this.username = username;
    }

    String username = "";

    public void setPassword(String password) {
        this.password = password;
    }

    String password = "";

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }

    String passwordRepeat = "";


    public void renderState() {
        super.renderState();
        int yPos = parent.height / 2 - parent.viewHeight / 2 + 14;
        int centerX = parent.width / 2;
        int centerY = parent.height / 2;


        var usernameInput = new TextInput(centerX, yPos, 200 - 8, 20, Text.translatable("register.username"),username);
        usernameInput.startListen(this::setUsername);
        children.add(usernameInput);

        yPos += 24;
        var passwordInput = new TextInput(centerX, yPos, 200 - 8, 20, Text.translatable("register.password"),password);
        passwordInput.setRenderTextProvider((string, i) -> Text.of("*".repeat(string.length())).asOrderedText());
        passwordInput.startListen(this::setPassword);
        children.add(passwordInput);

        yPos += 24;
        var passwordRepeatInput = new TextInput(centerX, yPos, 200 - 8, 20, Text.translatable("register.passwordrepeat"),passwordRepeat);
        passwordRepeatInput.setRenderTextProvider((string, i) -> Text.of("*".repeat(string.length())).asOrderedText());
        passwordRepeatInput.startListen(this::setPasswordRepeat);
        children.add(passwordRepeatInput);

        yPos += 24;
        children.add(new Button
                .Builder()
                .text(Text.translatable("register.confirm"))
                .width(200 - 8)
                .position(centerX, yPos)
                .action(this::register)
                .build());


        yPos += 24;
        children.add(new Button
                .Builder()
                .text(Text.translatable("register.cancel"))
                .width(98)
                .position(centerX, centerY + parent.viewHeight / 2 - 16)
                .action(this::backToLogin)
                .build());

        yPos += 16;
        this.warningText = new TextComponent(Text.empty(), centerX, yPos);
        children.add(this.warningText);

    }

    private void backToLogin() {
        parent.setState(new LoginState(parent));
    }

    private void register() {
        if (username.isBlank()) {
            warningText.setText(Text.translatable("register.username_empty"));
            return;
        }
        if (password.isBlank() || passwordRepeat.isBlank()) {
            warningText.setText(Text.translatable("register.password_empty"));
            return;
        }
        if (!password.equals(passwordRepeat)) {
            warningText.setText(Text.translatable("register.password_mismatch"));
            return;
        }
        SimpleRequestQueue.getInstance().request("register", Map.of(
            "username", RegisterState.this.username,
            "password", RegisterState.this.password
        ),(responsePacket, body)->{
            if(responsePacket.status() == Status.SUCCESS){
                parent.setState(new LoggedInState(parent, body.get("token").getAsString()));
            }
            else {
                warningText.setText(Text.translatable("register.server."+body.get("error").getAsString()));
            }
        });

    }
}
