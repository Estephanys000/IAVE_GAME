package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

/**
 * Simula a leitura de uma bicicleta ergométrica via teclado.
 *
 * O jogador alterna entre LEFT e RIGHT para simular as pedaladas.
 * Cada alternância completa conta como uma pedalada.
 *
 * Teclas:
 *   ← (LEFT)  — pedal esquerdo
 *   → (RIGHT) — pedal direito
 */
public class IoTSimulador {

    public static final int TECLA_ESQUERDA = Keys.LEFT;
    public static final int TECLA_DIREITA  = Keys.RIGHT;

    private static final float TEMPO_PARADO   = 3f;  // segundos sem pedalar = parado
    private static final float JANELA_RPM     = 5f;  // janela para cálculo de RPM
    private static final int   MAX_HISTORICO  = 20;  // amostras para consistência

    private boolean ultimaFoiEsquerda = false;
    private boolean iniciado          = false;  // aguarda primeira tecla

    private float tempoTotal           = 0f;
    private float tempoUltimaPedalada  = -1f;
    private float pedalasNaJanela      = 0f;
    private float tempoJanela          = 0f;

    private int   totalPedaladas = 0;
    private float rpm            = 0f;
    private float consistencia   = 0f;
    private boolean pedalando    = false;

    private final float[] historico = new float[MAX_HISTORICO];
    private int indiceHistorico  = 0;
    private int tamanhoHistorico = 0;

    public void update(float delta) {
        tempoTotal  += delta;
        tempoJanela += delta;

        boolean pressionouEsq = Gdx.input.isKeyJustPressed(TECLA_ESQUERDA);
        boolean pressionouDir = Gdx.input.isKeyJustPressed(TECLA_DIREITA);

        if (!iniciado) {
            // Aceita qualquer tecla como primeira pedalada
            if (pressionouEsq) {
                ultimaFoiEsquerda = true;
                iniciado = true;
                registrarPedalada();
            } else if (pressionouDir) {
                ultimaFoiEsquerda = false;
                iniciado = true;
                registrarPedalada();
            }
        } else {
            // Só conta se alternar do lado anterior
            if (pressionouEsq && !ultimaFoiEsquerda) {
                ultimaFoiEsquerda = true;
                registrarPedalada();
            } else if (pressionouDir && ultimaFoiEsquerda) {
                ultimaFoiEsquerda = false;
                registrarPedalada();
            }
        }

        // Verifica parada
        if (tempoUltimaPedalada >= 0 && (tempoTotal - tempoUltimaPedalada) > TEMPO_PARADO) {
            pedalando = false;
            rpm = 0f;
        }

        // Atualiza RPM pela janela deslizante
        if (tempoJanela >= JANELA_RPM) {
            rpm = (pedalasNaJanela / tempoJanela) * 60f;
            pedalasNaJanela = 0f;
            tempoJanela = 0f;
        }
    }

    private void registrarPedalada() {
        if (tempoUltimaPedalada >= 0f) {
            float intervalo = tempoTotal - tempoUltimaPedalada;
            if (intervalo < TEMPO_PARADO) {
                historico[indiceHistorico % MAX_HISTORICO] = intervalo;
                indiceHistorico++;
                tamanhoHistorico = Math.min(tamanhoHistorico + 1, MAX_HISTORICO);
                calcularConsistencia();

                // RPM imediato: cada press é meio ciclo
                rpm = 60f / (intervalo * 2f);
            }
        }

        tempoUltimaPedalada = tempoTotal;
        totalPedaladas++;
        pedalasNaJanela++;
        pedalando = true;
    }

    private void calcularConsistencia() {
        if (tamanhoHistorico < 3) {
            consistencia = 0f;
            return;
        }
        float soma = 0f;
        for (int i = 0; i < tamanhoHistorico; i++) soma += historico[i];
        float media = soma / tamanhoHistorico;

        float variancia = 0f;
        for (int i = 0; i < tamanhoHistorico; i++) {
            float d = historico[i] - media;
            variancia += d * d;
        }
        float desvio = (float) Math.sqrt(variancia / tamanhoHistorico);
        float cv = (media > 0f) ? (desvio / media) : 1f;
        consistencia = Math.max(0f, 1f - cv);
    }

    public float   getRpm()             { return rpm; }
    public float   getConsistencia()    { return consistencia; }
    public int     getTotalPedaladas()  { return totalPedaladas; }
    public boolean isPedalando()        { return pedalando; }
    public float   getTempoTotal()      { return tempoTotal; }

    public void reset() {
        totalPedaladas   = 0;
        rpm              = 0f;
        consistencia     = 0f;
        pedalando        = false;
        iniciado         = false;
        tempoTotal       = 0f;
        tempoUltimaPedalada = -1f;
        tempoJanela      = 0f;
        pedalasNaJanela  = 0f;
        tamanhoHistorico = 0;
        indiceHistorico  = 0;
    }
}
