package io.github.jumperonjava.kpz_atm_mod.client.ui.state;

import io.github.jumperonjava.kpz_atm_mod.client.SimpleRequestQueue;
import io.github.jumperonjava.kpz_atm_mod.client.ui.AtmScreen;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.TextComponent;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.TextInput;
import io.github.jumperonjava.kpz_atm_mod.endpoints.Status;
import net.minecraft.text.Text;

import java.util.Map;

public class TransferReceiverInputState extends MoneyInputState {
    private final String token;

    private String receiverUsername;

    public TransferReceiverInputState(AtmScreen parent, String token) {
        super(parent, "transfer", token);
        this.token = token;
    }

    @Override
    public void renderState() {
        children.clear();
        super.renderState();

        int yPos = parent.height / 2 - parent.viewHeight / 2 + 14;

        var textInput = new TextInput(centerX, yPos, parent.viewWidth - 8, 20, Text.translatable("transfer.receiver.input"));
        textInput.startListen((receiverUsername) -> {
            this.receiverUsername = receiverUsername;
        });

        children.add(textInput);
    }

    @Override
    protected void confirm() {
        SimpleRequestQueue.getInstance().request("transfer", Map.of("token", token, "amount", value, "receiver", receiverUsername), ((response, body) ->
        {
            if (response.status() == Status.SUCCESS) {
                parent.setState(new LoggedInState(parent, token));
            } else {
                warningText.setText(Text.translatable("transfer.server." + body.get("error").getAsString()));
            }
        }));
    }

    @Override
    protected void cancel() {
        parent.setState(new LoggedInState(parent, token));
    }
}
