package io.github.jumperonjava.kpz_atm_mod.client.ui.state;

import io.github.jumperonjava.kpz_atm_mod.client.ui.AtmScreen;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.Button;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.TextInput;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class RegisterState extends GenericState {
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


    public void initComponents() {
        children = new ArrayList<>();
        int yPos = parent.height / 2 - parent.viewHeight / 2 + 14;


        var usernameInput = new TextInput(parent.width/2,yPos,200-8,20,Text.translatable("register.username"));
        usernameInput.startListen(this::setUsername);
        children.add(usernameInput);

        yPos += 24;
        var passwordInput = new TextInput(parent.width/2,yPos,200-8,20,Text.translatable("register.password"));
        passwordInput.setRenderTextProvider((string,i)-> Text.of("*".repeat(string.length())).asOrderedText());
        passwordInput.startListen(this::setPassword);
        children.add(passwordInput);

        yPos += 24;
        var passwordRepeatInput = new TextInput(parent.width/2,yPos,200-8,20,Text.translatable("register.passwordrepeat"));
        passwordRepeatInput.setRenderTextProvider((string,i)-> Text.of("*".repeat(string.length())).asOrderedText());
        passwordRepeatInput.startListen(this::setPasswordRepeat);
        children.add(passwordRepeatInput);

        yPos += 24;
        children.add(new Button
                .Builder()
                .text(Text.literal("register"))
                .width(200-8)
                .position(parent.width / 2, yPos)
                .action(this::register)
                .build());


        yPos += 24;
        children.add(new Button
                .Builder()
                .text(Text.literal("Back to login"))
                .width(98)
                .position(parent.width / 2, yPos)
                .action(this::backToLogin)
                .build());
    }

    private void backToLogin() {
        parent.setState(new LoginState(parent));
    }

    private void register() {

    }
}
