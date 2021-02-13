package me.comu.exeter.commands.image;

import me.comu.exeter.core.Config;
import me.comu.exeter.core.Core;
import me.comu.exeter.interfaces.ICommand;
import me.comu.exeter.utility.Utility;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class GrayScaleImageCommand implements ICommand {


    @Override
    public void handle(List<String> args, GuildMessageReceivedEvent event) {
        String url = "https://api.alexflipnote.dev/filter/b&w?image=";
        if (args.isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            url = url + event.getAuthor().getEffectiveAvatarUrl();
        }
        if (!event.getMessage().getMentionedMembers().isEmpty()) {
            url = url + event.getMessage().getMentionedMembers().get(0).getUser().getEffectiveAvatarUrl().replace(".gif", ".png");
        } else if (!args.isEmpty() && !Utility.isUrl(args.get(0))) {
            event.getChannel().sendMessage("I couldn't resolve the specified URL").queue();
            return;
        }

        if (!args.isEmpty() && Utility.isUrl(args.get(0))) {
            url = url + args.get(0).replace(".gif", ".png");
        } else if (!event.getMessage().getAttachments().isEmpty()) {
            url = url + event.getMessage().getAttachments().get(0).getUrl();
        }
            try {
                BufferedImage img = ImageIO.read(new URL(url));
                File file = new File("cache/downloaded.png");
                ImageIO.write(img, "png", file);
                event.getChannel().sendFile(file, "swag.png").queue(lol -> Config.clearCacheDirectory());
            } catch (Exception ex) {
                Config.clearCacheDirectory();
                event.getChannel().sendMessage("Something went wrong, try again later").queue();
            }

    }
/*        if (Utility.beingProcessed) {
            event.getChannel().sendMessage("An image is already being processed, please wait.").queue();
            return;
        }
        if (event.getMessage().getAttachments().isEmpty()) {
            if (args.isEmpty()) {
                event.getChannel().sendMessage("Please insert an image link to manipulate").queue();
                return;
            }
            Utility.beingProcessed = true;
            event.getChannel().sendMessage("`Processing Image...`").queue(message -> {
                try {
                    int random = new Random().nextInt(1000);
                    int newRandom = new Random().nextInt(1000);
                    Utility.saveImage(args.get(0), "cache", "image" + random);
                    File file = new File("cache/image" + random + ".png");
                    BufferedImage image = ImageIO.read(file);
                    CompletableFuture.supplyAsync(() -> image)
                            .thenApply(this::grayScaleImg)
                            .completeOnTimeout(null, 10, TimeUnit.SECONDS)
                            .thenAccept(processedImage -> {
                                if (processedImage == null) {
                                    message.editMessage("Processing thread timed out.").queue();
                                    Config.clearCacheDirectory();
                                    Utility.beingProcessed = false;
                                } else {
                                    try {
                                        File newFilePNG = new File("cache/image" + newRandom + ".png");
                                        ImageIO.write(processedImage, "png", newFilePNG);
                                        message.delete().queue();
                                        event.getChannel().sendFile(newFilePNG, "swag.png").queue(lol -> Config.clearCacheDirectory());
                                        Utility.beingProcessed = false;
                                    } catch (Exception ignored) {
                                        message.editMessage("Something went wrong with processing the image").queue();
                                        Utility.beingProcessed = false;
                                    }
                                }
                            });
                } catch (Exception ignored) {
                    message.editMessage("Something went wrong with processing the image").queue();
                    Utility.beingProcessed = false;
                }

            });
        } else {
            Utility.beingProcessed = true;
            event.getChannel().sendMessage("`Processing Image...`").queue(message -> {
                try {
                    int random = new Random().nextInt(1000);
                    int newRandom = new Random().nextInt(1000);
                    Utility.saveImage(event.getMessage().getAttachments().get(0).getUrl(), "cache", "image" + random);
                    File file = new File("cache/image" + random + ".png");
                    BufferedImage image = ImageIO.read(file);
                    CompletableFuture.supplyAsync(() -> image)
                            .thenApply(this::grayScaleImg)
                            .completeOnTimeout(null, 10, TimeUnit.SECONDS)
                            .thenAccept(processedImage -> {
                                if (processedImage == null) {
                                    message.editMessage("Processing thread timed out.").queue();
                                    Config.clearCacheDirectory();
                                    Utility.beingProcessed = false;
                                } else {
                                    try {
                                        File newFilePNG = new File("cache/image" + newRandom + ".png");
                                        ImageIO.write(processedImage, "png", newFilePNG);
                                        message.delete().queue();
                                        event.getChannel().sendFile(newFilePNG, "swag.png").queue(lol -> Config.clearCacheDirectory());
                                        Utility.beingProcessed = false;
                                    } catch (Exception ignored) {
                                        message.editMessage("Something went wrong with processing the image").queue();
                                        Utility.beingProcessed = false;
                                    }
                                }
                            });
                } catch (Exception ex) {
                    message.editMessage("Something went wrong with processing the image").queue();
                    Utility.beingProcessed = false;
                }

            });
        }

        Config.clearCacheDirectory();
    }

    private BufferedImage grayScaleImg(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = image.getRGB(x, y);
                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;
                int avg = (r + g + b) / 3;
                p = (a << 24) | (avg << 16) | (avg << 8) | avg;
                image.setRGB(x, y, p);
            }
        }
        return image;
    }*/

    @Override
    public String getHelp() {
        return "Makes the picture black and white\n`" + Core.PREFIX + getInvoke() + " `\nAliases: `" + Arrays.deepToString(getAlias()) + "`";
    }

    @Override
    public String getInvoke() {
        return "grayscale";
    }

    @Override
    public String[] getAlias() {
        return new String[]{"blackandwhite", "blackwhite", "blacknwhite", "grayscaleimage", "grayscaleimg"};
    }

    @Override
    public Category getCategory() {
        return Category.IMAGE;
    }
}