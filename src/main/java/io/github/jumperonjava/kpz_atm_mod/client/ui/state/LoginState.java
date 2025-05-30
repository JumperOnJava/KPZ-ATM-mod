package io.github.jumperonjava.kpz_atm_mod.client.ui.state;

import io.github.jumperonjava.kpz_atm_mod.client.SimpleRequestQueue;
import io.github.jumperonjava.kpz_atm_mod.client.ui.AtmScreen;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.Button;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.TextInput;
import io.github.jumperonjava.kpz_atm_mod.server.Status;
import net.minecraft.text.Text;

import java.util.Map;

public class LoginState extends GenericState {
    public LoginState(AtmScreen parent) {
        super(parent);
    }

    @Override
    public Text title() {
        return Text.translatable("login.title");
    }

    public void setUsername(String username) {
        this.username = username;
    }

    String username = "";

    public void setPassword(String password) {
        this.password = password;
    }

    String password = "";

    public void renderState() {
        super.renderState();
        int yPos = parent.height / 2 - parent.viewHeight / 2 + 14;


        var usernameInput = new TextInput(parent.width / 2, yPos, 200 - 8, 20, Text.translatable("login.username"),username);
        usernameInput.startListen(this::setUsername);
        children.add(usernameInput);

        yPos += 24;
        var passwordInput = new TextInput(parent.width / 2, yPos, 200 - 8, 20, Text.translatable("login.password"),password);
        passwordInput.setRenderTextProvider((string, i) -> Text.of("*".repeat(string.length())).asOrderedText());
        passwordInput.startListen(this::setPassword);
        children.add(passwordInput);

        yPos += 24;
        children.add(new Button
                .Builder()
                .text(Text.literal("Login"))
                .width(200 - 8)
                .position(parent.width / 2, yPos)
                .action(this::login)
                .build());


        yPos += 24;
        children.add(new Button
                .Builder()
                .text(Text.literal("Register"))
                .width(98)
                .position(parent.width / 2, yPos)
                .action(this::register)
                .build());
    }

    private void register() {
        parent.setState(new RegisterState(parent));
    }


    private void login() {
        parent.setState(new StubState(parent, "Loading"));

        //noinspection FieldMayBeFinal
        parent.requestQueue.request("login", Map.of(
                        "username", this.username,
                        "password", this.password
                ),
                (response, data) -> {
                    if(response.status() == Status.SUCCESS){
                        parent.setState(new LoggedInState(parent,data.get("token").getAsString()));
                    }
                    else {
                        parent.setState(this);
                    }
                });
    }
}
