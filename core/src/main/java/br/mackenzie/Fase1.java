package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

// ========================================================
// CLASSE PRINCIPAL DA FASE (Deve ser o nome do arquivo: Fase1.java)
// ========================================================
public class Fase1 extends ScreenAdapter {

    private final Main jogo;

    // ADICIONADO: Estado WIN incluído no GameState
    private enum GameState { WAITING, PLAYING, GAME_OVER, WIN }

    private static final int MAX_LIVES = 3;
    private static final int SCORE_VITORIA = 40; // Pontuação necessária para vencer

    public static final float LEFT_WALL  = 1.5f;
    public static final float RIGHT_WALL = 6.5f;

    private GameState gameState = GameState.WAITING;

    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private OrthographicCamera hudCamera;

    private Texture playerTex, playerLeftTex, playerRightTex;
    private Texture bgNearTex;
    private Texture inimigo1Tex, inimigo2Tex, inimigo3Tex;
    private Texture powerTex;
    private Texture shieldTex;
    private Texture startGameTex, gameOverTex, heartTex, winTex; // ADICIONADO: winTex

    private PlayerShip player;
    private Array<inimigo3> enemies;
    private Array<PowerUp> activePowerUps;
    private Array<Shield> activeShields;

    private float spawnTimer;
    private int score = 0;
    private int lives = 3;
    private float bgNearY = 0f;

    private Sound dropSound;
    private Music music;
    private BitmapFont font;

    private int screenW, screenH;

    public Fase1(Main jogo) {
        this.jogo = jogo;
    }

    @Override
    public void show() {
        spriteBatch = new SpriteBatch();
        viewport    = new FitViewport(8, 5);

        screenW = Gdx.graphics.getWidth();
        screenH = Gdx.graphics.getHeight();

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, screenW, screenH);

        playerTex      = new Texture("player.png");
        playerLeftTex  = new Texture("player_left.png");
        playerRightTex = new Texture("player_right.png");

        bgNearTex   = new Texture("background_near.png");
        inimigo1Tex = new Texture("inimigo_pequeno.png");
        inimigo2Tex = new Texture("inimigo_grande.png");
        inimigo3Tex = new Texture("inimigo3.png");
        powerTex    = new Texture("drop.png");
        shieldTex   = new Texture("shield.png");

        startGameTex = new Texture("start_game.png");
        gameOverTex  = new Texture("game_over.png");
        heartTex     = new Texture("heart.png");
        winTex       = new Texture("win.png"); // ADICIONADO: Inicialização do asset de vitória

        font      = new BitmapFont();
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));

        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        music.setLooping(true);
        music.play();

        initGame();
    }

    private void initGame() {
        player = new PlayerShip(playerTex, playerLeftTex, playerRightTex, 3.5f, 0.2f);

        enemies        = new Array<>();
        activePowerUps = new Array<>();
        activeShields  = new Array<>();

        spawnTimer = 0;
        score      = 0;
        lives      = MAX_LIVES;
        bgNearY    = 0f;
    }

    @Override
    public void render(float delta) {
        switch (gameState) {
            case WAITING:
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
                    gameState = GameState.PLAYING;
                break;

            case PLAYING:
                updateParallax(delta);
                player.update(delta);
                spawnObjects(delta);
                updateGameObjects(delta);

                // ADICIONADO: Verificação da condição de vitória
                if (score >= SCORE_VITORIA) {
                    gameState = GameState.WIN;
                }
                break;

            case GAME_OVER:
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    initGame();
                    gameState = GameState.WAITING;
                }
                break;

            case WIN:
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    Gdx.input.setInputProcessor(null);
                    jogo.setScreen(new Fase2(jogo));
                    return;
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    Gdx.input.setInputProcessor(null);
                    jogo.setScreen(new MenuPrincipal(jogo));
                    return;
                }
                break;
        }

        ScreenUtils.clear(Color.BLACK);

        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();
        spriteBatch.draw(bgNearTex, 0, bgNearY,       8, 5);
        spriteBatch.draw(bgNearTex, 0, bgNearY + 5f,  8, 5);

        if (gameState != GameState.WAITING) {
            for (inimigo3 e  : enemies)        e.draw(spriteBatch);
            for (PowerUp  p  : activePowerUps) p.draw(spriteBatch);
            for (Shield   s  : activeShields)  s.draw(spriteBatch);
            player.draw(spriteBatch);
        }
        spriteBatch.end();

        hudCamera.update();
        spriteBatch.setProjectionMatrix(hudCamera.combined);
        spriteBatch.begin();
        drawHUD();
        spriteBatch.end();
    }

    private void updateParallax(float delta) {
        bgNearY -= 2.0f * delta;
        if (bgNearY <= -5f) bgNearY += 5f;
    }

    private void drawHUD() {
        float imgW = screenW * 0.60f;
        float imgH = imgW * 0.375f;
        float imgX = (screenW - imgW) / 2f;
        float imgY = (screenH - imgH) / 2f;

        switch (gameState) {
            case WAITING:
                spriteBatch.draw(startGameTex, imgX, imgY, imgW, imgH);
                break;
            case PLAYING:
                drawScore();
                drawLiveHearts();
                break;
            case GAME_OVER:
                drawScore();
                drawLiveHearts();
                spriteBatch.draw(gameOverTex, imgX, imgY, imgW, imgH);
                break;
            case WIN:
                // ADICIONADO: Renderização da HUD de Vitória
                drawScore();
                drawLiveHearts();
                spriteBatch.draw(winTex, imgX, imgY, imgW, imgH);
                break;
        }
    }

    private void drawScore() {
        font.setColor(Color.RED);
        font.getData().setScale(3.0f);
        font.draw(spriteBatch, "Score: " + score, 20, screenH - 20);
        font.getData().setScale(1.0f);
    }

    private void drawLiveHearts() {
        float heartSize = 60f;
        float margin    = 10f;
        float startX    = screenW - (lives * (heartSize + margin)) - 10;
        float y         = screenH - heartSize - 10;

        for (int i = 0; i < lives; i++)
            spriteBatch.draw(heartTex, startX + i * (heartSize + margin), y, heartSize, heartSize);
    }

    private void spawnObjects(float delta) {
        spawnTimer += delta;
        if (spawnTimer < 1.5f) return;
        spawnTimer = 0;

        float x    = MathUtils.random(LEFT_WALL, RIGHT_WALL - 1.0f);
        float roll = MathUtils.random();

        if (roll < 0.5f) {
            enemies.add(new inimigo3(inimigo1Tex, x, 5f, 0.8f, 0, -2.0f));
        } else if (roll < 0.8f) {
            enemies.add(new inimigo3(inimigo2Tex,  x, 5f, 0.7f, 0, -2.0f));
        } else {
            enemies.add(new inimigo3(inimigo3Tex,   x, 5f, 1f, 0, -2.0f));
        }

        if (MathUtils.randomBoolean(0.1f)) {
            float px = MathUtils.random(LEFT_WALL, RIGHT_WALL - 0.8f);
            activePowerUps.add(new PowerUp(powerTex, px, 5f));
        }

        if (MathUtils.randomBoolean(0.01f)) {
            float sx = MathUtils.random(LEFT_WALL, RIGHT_WALL - 0.8f);
            activeShields.add(new Shield(shieldTex, sx, 5f));
        }
    }

    private void updateGameObjects(float delta) {
        Rectangle playerBounds = player.getBounds();

        // --- Inimigos/Obstáculos ---
        for (int i = enemies.size - 1; i >= 0; i--) {
            inimigo3 e = enemies.get(i);
            e.update(delta);

            Rectangle eb = e.getBounds();

            // COLISÃO: Aumenta o score se colidir e remove o inimigo
            if (!player.isInvincible() && eb.overlaps(playerBounds)) {
                score++;
                dropSound.play();
                enemies.removeIndex(i);
                continue;
            }

            // PASSOU DIRETO: Perde vida se o inimigo sair pela parte de baixo da tela
            if (eb.y < -1f) {
                lives--;
                player.hit();
                enemies.removeIndex(i);
                if (lives <= 0) gameState = GameState.GAME_OVER;
            }
        }

        // --- Power-ups de vida ---
        for (int i = activePowerUps.size - 1; i >= 0; i--) {
            PowerUp p = activePowerUps.get(i);
            p.update(delta);

            Rectangle pb = p.getBounds();
            if (pb.overlaps(playerBounds)) {
                dropSound.play();
                lives = Math.min(lives + 1, MAX_LIVES);
                activePowerUps.removeIndex(i);
            } else if (pb.y < -1f) {
                activePowerUps.removeIndex(i);
            }
        }

        // --- Escudos ---
        for (int i = activeShields.size - 1; i >= 0; i--) {
            Shield s = activeShields.get(i);
            s.update(delta);

            Rectangle sb = s.getBounds();
            if (sb.overlaps(playerBounds)) {
                dropSound.play();
                player.activateShield();
                activeShields.removeIndex(i);
            } else if (sb.y < -1f) {
                activeShields.removeIndex(i);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        screenW = width;
        screenH = height;
        hudCamera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        if (spriteBatch != null) spriteBatch.dispose();

        if (playerTex != null) playerTex.dispose();
        if (playerLeftTex != null) playerLeftTex.dispose();
        if (playerRightTex != null) playerRightTex.dispose();

        if (bgNearTex != null) bgNearTex.dispose();
        if (inimigo1Tex != null) inimigo1Tex.dispose();
        if (inimigo2Tex != null) inimigo2Tex.dispose();
        if (inimigo3Tex != null) inimigo3Tex.dispose();
        if (powerTex != null) powerTex.dispose();
        if (shieldTex != null) shieldTex.dispose();

        if (startGameTex != null) startGameTex.dispose();
        if (gameOverTex != null) gameOverTex.dispose();
        if (heartTex != null) heartTex.dispose();
        if (winTex != null) winTex.dispose(); // ADICIONADO: Desalocação da winTex

        if (dropSound != null) dropSound.dispose();
        if (music != null) music.dispose();
        if (font != null) font.dispose();
    }
}

// ========================================================
// CLASSES BASE E ENTIDADES (Mantidas intactas abaixo)
// ========================================================
abstract class GameObject {
    protected Sprite sprite;
    protected Rectangle bounds;

    public GameObject(Texture texture, float x, float y, float width, float height) {
        this.sprite = new Sprite(texture);
        this.sprite.setSize(width, height);
        this.sprite.setPosition(x, y);
        this.bounds = new Rectangle(x, y, width, height);
    }

    public abstract void update(float delta);

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public Rectangle getBounds() {
        bounds.set(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
        return bounds;
    }
}

class PlayerShip extends GameObject {
    private final float speed = 4f;

    private final Texture texIdle;
    private final Texture texLeft;
    private final Texture texRight;

    private float invincibleTimer = 0f;
    private float shieldTimer     = 0f;

    private static final float INVINCIBLE_DURATION = 0.1f;
    private static final float SHIELD_DURATION     = 6f;

    private static final float PLAYER_WIDTH  = 0.5f;
    private static final float PLAYER_HEIGHT = 0.5f;

    public PlayerShip(Texture idle, Texture left, Texture right, float x, float y) {
        super(idle, x, y, PLAYER_WIDTH, PLAYER_HEIGHT);
        this.texIdle = idle;
        this.texLeft = left;
        this.texRight = right;
    }

    @Override
    public void update(float delta) {
        if (invincibleTimer > 0) invincibleTimer -= delta;
        if (shieldTimer > 0)     shieldTimer     -= delta;

        Texture currentTexture = texIdle;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            sprite.translateX(speed * delta);
            currentTexture = texRight;
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            sprite.translateX(-speed * delta);
            currentTexture = texLeft;
        }

        sprite.setRegion(currentTexture);
        sprite.setX(MathUtils.clamp(sprite.getX(), Fase1.LEFT_WALL, Fase1.RIGHT_WALL - sprite.getWidth()));

        if (shieldTimer > 0) {
            float pulse = shieldTimer % 0.4f < 0.2f ? 0.6f : 1f;
            sprite.setColor(0.4f, 0.8f, 1f, pulse);
        } else if (invincibleTimer > 0) {
            float alpha = invincibleTimer % 0.2f < 0.1f ? 0.3f : 1f;
            sprite.setColor(1f, 1f, 1f, alpha);
        } else {
            sprite.setColor(Color.WHITE);
        }
    }

    public boolean isInvincible() {
        return invincibleTimer > 0 || shieldTimer > 0;
    }

    public void hit() {
        invincibleTimer = INVINCIBLE_DURATION;
    }

    public void activateShield() {
        shieldTimer = SHIELD_DURATION;
    }
}

class inimigo3 extends GameObject {
    protected Vector2 velocity;
    protected float hitboxSize;

    public inimigo3(Texture texture, float x, float y, float size, float vx, float vy) {
        super(texture, x, y, size, size);
        this.velocity  = new Vector2(vx, vy);
        this.hitboxSize = size * 0.6f;
    }

    @Override
    public void update(float delta) {
        sprite.translate(velocity.x * delta, velocity.y * delta);
    }

    @Override
    public Rectangle getBounds() {
        float offset = (sprite.getWidth() - hitboxSize) / 1.8f;
        bounds.set(
                sprite.getX() + offset,
                sprite.getY() + offset,
                hitboxSize,
                hitboxSize
        );
        return bounds;
    }
}

class PowerUp extends GameObject {
    public PowerUp(Texture texture, float x, float y) {
        super(texture, x, y, 0.5f, 0.5f);
    }

    @Override
    public void update(float delta) {
        sprite.translateY(-2.5f * delta);
    }
}

class Shield extends GameObject {
    public Shield(Texture texture, float x, float y) {
        super(texture, x, y, 0.5f, 0.5f);
    }

    @Override
    public void update(float delta) {
        sprite.translateY(-2.5f * delta);
    }
}