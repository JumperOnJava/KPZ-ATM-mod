package io.github.jumperonjava.kpz_atm_mod.client.ui.state;

import io.github.jumperonjava.kpz_atm_mod.client.SimpleRequestQueue;
import io.github.jumperonjava.kpz_atm_mod.client.ui.AtmScreen;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.Button;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.TextComponent;
import io.github.jumperonjava.kpz_atm_mod.endpoints.Status;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;

public class MoneyInputState extends GenericState {
    private final String token;
    private final String endpoint;
    private TextComponent text;
    protected int value;
    protected TextComponent warningText;

    public MoneyInputState(AtmScreen parent, String endpoint, String token) {
        super(parent);
        this.token = token;
        this.endpoint = endpoint;
    }

    @Override
    public Text title() {
        return Text.translatable(endpoint + ".title");
    }

    @Override
    public void renderState() {
        super.renderState();

        int yPos = parent.height / 2 - parent.viewHeight / 2 + 38;

        children.add(new TextComponent(Text.translatable(endpoint + ".amount"), centerX, yPos));

        yPos += 16;

        this.text = new TextComponent(Text.empty(), centerX, yPos);
        children.add(text);

        var bulider = new Button
                .Builder()
                .width(30);

        yPos += 24;
        bulider.centerY(yPos);

        var matrix = List.of(List.of(1, 8, 64), List.of(-1, -8, -64));

        for (int i = 0; i < matrix.size(); i++) {
            var row = matrix.get(i);
            for (int j = 0; j < row.size(); j++) {
                var element = row.get(j);
                children.add(
                        bulider
                                .centerX(centerX - 32 + j * 32)
                                .centerY(yPos)
                                .action(() -> changeValue(element))
                                .text(Text.literal("%+d".formatted(element)))
                                .build()
                );
            }
            yPos += 22;
        }
        bulider.width(46);
        children.add(bulider
                .centerY(yPos)
                .centerX(centerX - 24)
                .text(Text.translatable(endpoint + ".cancel"))
                .action(this::cancel).build());
        children.add(bulider
                .centerY(yPos)
                .centerX(centerX + 24)
                .text(Text.translatable(endpoint + ".confirm"))
                .action(this::confirm).build());

        yPos += 16;
        this.warningText = new TextComponent(Text.empty(), centerX, yPos);
        children.add(warningText);
        changeValue(value);
    }

    protected void confirm() {
        SimpleRequestQueue.getInstance().request(endpoint, Map.of(
                "token", MoneyInputState.this.token,
                "amount", MoneyInputState.this.value
        ), (packet, data) -> {
            if (packet.status() == Status.SUCCESS) {
                parent.setState(new LoggedInState(parent, token));
            } else {
                warningText.setText(Text.translatable(endpoint + ".error." + data.get("error").getAsString()));
            }
        });
    }

    protected void cancel() {
        parent.setState(new LoggedInState(parent, token));
    }

    private void changeValue(int change) {
        this.value += change;
        if (value < 0) {
            value = 0;
        }
        text.setText(Text.literal(String.valueOf(value)));
    }
}
