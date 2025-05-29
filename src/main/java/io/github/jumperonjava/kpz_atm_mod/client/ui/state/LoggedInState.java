package io.github.jumperonjava.kpz_atm_mod.client.ui.state;

import io.github.jumperonjava.kpz_atm_mod.client.SimpleRequestQueue;
import io.github.jumperonjava.kpz_atm_mod.client.ui.AtmScreen;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.Button;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.TextComponent;
import io.github.jumperonjava.kpz_atm_mod.endpoints.Status;
import net.minecraft.text.Text;

import java.util.Map;

public class LoggedInState extends GenericState {

    String token;

    public LoggedInState(AtmScreen parent, String token) {
        super(parent);
        this.token = token;
    }

    @Override
    public Text title() {
        return Text.translatable("loggedin.title");
    }


    public void renderState() {
        int yPos = parent.height / 2 - parent.viewHeight / 2 + 14;

        var balance = new TextComponent(Text.translatable("loggedin.balance.fetching"),centerX, yPos);
        children.add(balance);

        SimpleRequestQueue.getInstance().request("balance", Map.of("token",token),(p,body)->{
            if(p.status() == Status.SUCCESS){
                balance.setText(Text.translatable("loggedin.balance",(int)(body.get("balance").getAsDouble())));
            }
            else if(p.status() == Status.ERROR){
                balance.setText(Text.translatable("loggedin.balance",(body.get("error").getAsString())));
            }
        });

        var bulider = new Button
                .Builder()
                .width(94);

        yPos += 24;
        bulider.centerY(yPos);

        children.add(bulider
                .text(Text.literal("Deposit"))
                .centerX(centerX - 49)
                .action(this::deposit)
                .build());


        children.add(bulider
                .text(Text.literal("Withdraw"))
                .centerX(centerX + 49)
                .action(this::withdraw)
                .build());

        yPos += 24;
        bulider.centerY(yPos);

        children.add(bulider
                .text(Text.literal("Transfer"))
                .centerX(centerX - 49)
                .action(this::transfer)
                .build());


        children.add(bulider
                .text(Text.literal("History"))
                .centerX(centerX + 49)
                .action(this::history)
                .build());

        yPos += 24;
        bulider.centerY(yPos);

        children.add(bulider
                .text(Text.literal("Logout"))
                .centerX(centerX)
                .width(192)
                .action(this::logout)
                .build());

    }

    private void deposit() {
        parent.setState(new MoneyInputState(parent, "deposit", token));
    }

    private void withdraw() {
        parent.setState(new MoneyInputState(parent, "withdraw", token));
    }

    private void transfer() {
        parent.setState(new TransferReceiverInputState(parent, token));
    }

    private void history() {

    }

    private void logout() {
        parent.setState(new LoginState(parent));
    }
}
