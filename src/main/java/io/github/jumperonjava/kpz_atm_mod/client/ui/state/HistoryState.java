package io.github.jumperonjava.kpz_atm_mod.client.ui.state;

import io.github.jumperonjava.kpz_atm_mod.client.SimpleRequestQueue;
import io.github.jumperonjava.kpz_atm_mod.client.ui.AtmScreen;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.Button;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.Component;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.TextComponent;
import io.github.jumperonjava.kpz_atm_mod.server.Status;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.Map;

public class HistoryState extends GenericState {
    private final String token;
    protected TextComponent warningText;

    public HistoryState(AtmScreen parent, String token) {
        super(parent);
        this.token = token;
    }

    @Override
    public Text title() {
        return Text.translatable("history.title");
    }

    @Override
    public void renderState() {
        super.renderState();

        children.add(new Button
                .Builder()
                .width(parent.viewWidth - 8)
                .centerX(centerX)
                .centerY(centerY + parent.viewHeight / 2 - 14)
                .text(Text.translatable("history.cancel"))
                .action(this::cancel).build());

        var list = new HistoryScrollList();
        children.add(list);
        SimpleRequestQueue.getInstance().request(
                "history", Map.of("token", token), (response, body) -> {
                    list.children().clear();
                    if (response.status() == Status.SUCCESS) {
                        body.get("history").getAsJsonArray().forEach(entry ->
                                list.children().add(new HistoryScrollList.HistoryElement(
                                                entry.getAsJsonObject().get("from").getAsString(),
                                                entry.getAsJsonObject().get("to").getAsString(),
                                                (int) entry.getAsJsonObject().get("amount").getAsDouble(),
                                                entry.getAsJsonObject().get("type").getAsString(),
                                                entry.getAsJsonObject().get("date").getAsString(),
                                                parent.viewWidth - 26
                                        )
                                ));
                    } else if (response.status() == Status.ERROR) {
                        warningText.setText(Text.translatable("history.error" + body.get("error").getAsString()));
                    }
                }
        );

        this.warningText = new TextComponent(Text.empty(), centerX, centerY);
        children.add(warningText);
    }

    protected void cancel() {
        parent.setState(new LoggedInState(parent, token));
    }

    class HistoryScrollList extends EntryListWidget<HistoryScrollList.HistoryElement> implements Component {


        public HistoryScrollList() {
            super(MinecraftClient.getInstance(), parent.viewWidth - 8, parent.viewHeight - 36, centerY - parent.viewHeight / 2 + 6, 40);
            setX(getRowLeft());
        }

        @Override
        public int getRowLeft() {
            return centerX - parent.viewWidth / 2 + 4;
        }

        @Override
        public int getRowRight() {
            return super.getRowRight();
        }

        @Override
        protected int getScrollbarX() {
            return super.getRowRight() - 34;
        }

        static class HistoryElement extends EntryListWidget.Entry<HistoryElement> {
            private final String from;
            private final String to;
            private final int amount;
            private final String type;
            private final String date;
            private final int width;

            HistoryElement(String from, String to, int amount, String type, String date, int width) {
                this.from = from;
                this.to = to;
                this.amount = amount;
                this.type = type;
                this.date = date;
                this.width = width;
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
                if(index % 2 == 1){
                    context.fill(x,y-2,x+entryWidth,y+40-2,0x7f000000);
                }
                context.fill(x+1,y-2,x+entryWidth-2,y-1,0x3fFFFFFF);
                var text = Text.translatable("history." + type, from, to, amount);
                context.drawWrappedTextWithShadow(MinecraftClient.getInstance().textRenderer, text, x + 4, y, width, Colors.WHITE);
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(date), x + 4, y + 30-1, Colors.WHITE);
            }
        }


        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            builder.put(NarrationPart.TITLE, Text.empty());
        }
    }

}
