package agents;
//Dogecoin
//Autors: Pau Bonet, Joan Martorell, Maurici Ruiz 

import java.util.Random;
public class Bitxo11 extends Agent {
    
    static final int PARET = 0;
    static final int BITXO = 2;
    static final int RES = -1;
    static final int ESQUERRA = 0;
    static final int CENTRAL = 1;
    static final int DRETA = 2;
    static final int RECURS_GRUP = 100;

    private int repetir = 0;
    private int pausaDispar = 0;
    private int colisionado = 0;
    private int nLlançaments = 0;
    private int iEnemic = 0;
    private int capObjectes = 0;

    Estat estat;
    Random r = new Random();

    public Bitxo11(Agents pare) {
        super(pare, "Dogecoin", "imatges/dogecoin.gif");
    }

    @Override
    public void inicia() {
        // atributsAgents(v,w,dv,av,ll,es,hy)
        int cost = atributsAgent(6, 5, 600, 55, 32, 5, 4);
        System.out.println("Cost total:" + cost);

        // Inicialització de variables que utilitzaré al meu comportament
    }

    @Override
    public void avaluaComportament() {
        estat = estatCombat();
        int state;
        if (pausaDispar > 0) {
            mira(estat.objectes[iEnemic]);
            pausaDispar--;
        } else {
            if (colisionado > 0) {
                state = 0;
                colisionado--;
            } else {
                state = detectarAnyRecurs();
            }
            switch (state) {
                case -1:    //anada normal
                    capObjectes++;
                    detectarPared(14);
                    movRandom();
                    proteccioLlançament();
                    salvacio();
                    disparaEnemic();
                    novaUbicacio();
                    break;
                case 0:     //veu recurs
                    capObjectes = 0;
                    disparaEnemic();
                    detectarPared(14);
                    if (estat.enCollisio && colisionado == 0) {
                        colisionado = 5;
                        break;
                    } else if (estat.enCollisio && colisionado > 0) {
                        enrere();
                        gira(40);
                    } else {
                        endavant();
                    }
                    break;
                case 1:     //veu recurs enemic
                    capObjectes = 0;
                    disparaEnemic();
                    if (!detectarPared(40)) {
                        llança();
                    }
                    break;
            }
        }
    }

    /**
     * Moviment random que fa anar cap endavant a l'agent
     */
    private void movRandom() {
        if (repetir == 0) {
            endavant();
            if (r.nextBoolean()) {
                gira(-45);
            } else {
                gira(45);
            }
            repetir = 20;
        } else {
            repetir--;
        }
    }

    /**
     * Metode que detecta pared i si esta davant d'una gira a l'atzar per
     * sortejarla, retornara si hi havia una pared o no l'havia
     *
     * @param dist
     * @return
     */
    private boolean detectarPared(int dist) {
        for (int i = 0; i < estat.objecteVisor.length; i++) {
            r = new Random();
            if (estat.objecteVisor[i] == PARET && estat.distanciaVisors[i] < dist) {
                if (r.nextBoolean()) {
                    gira(r.nextInt());
                } else {
                    gira(-r.nextInt());
                }
                return true;
            } else {
                endavant();
            }
        }
        return false;
    }

    /**
     * metode que detecta els recursos que hi ha a la partida, tant els escuts,
     * el menjar de l'agent o els recursos enemics
     *
     * @return
     */
    private int detectarAnyRecurs() {
        if (estat.veigAlgunRecurs || estat.veigAlgunEscut) {
            int minDist = 9999;
            int recurs = -1;
            int recursEnemic = -1;
            for (int i = 0; estat.objectes[i] != null && i < estat.objectes.length; i++) {
                if (estat.objectes[i].agafaTipus() == RECURS_GRUP + getId()
                        || estat.objectes[i].agafaTipus() == Estat.ESCUT) {
                    if (estat.objectes[i].agafaDistancia() < minDist) {
                        minDist = estat.objectes[i].agafaDistancia();
                        recurs = i;
                    }
                } else if ((estat.objectes[i].agafaTipus() != RECURS_GRUP + getId())
                        && (estat.objectes[i].agafaTipus() >= RECURS_GRUP)
                        && (estat.objectes[i].agafaTipus() <= RECURS_GRUP + estat.numBitxos)) {
                    if (estat.objectes[i].agafaDistancia() < minDist) {
                        minDist = estat.objectes[i].agafaDistancia();
                        recursEnemic = i;
                    }
                }
            }
            if (recurs != -1) {
                atura();
                mira(estat.objectes[recurs]);
                return 0;
            } else if ((recursEnemic != -1) && (estat.objectes[recursEnemic].agafaDistancia() < 200)) {
                atura();
                mira(estat.objectes[recursEnemic]);
                return 1;
            }
        }
        return -1;
    }

    /**
     * metode que vigila si rep llanáments enemics per la part on te visió
     * l'agent
     */
    private void proteccioLlançament() {
        if (estat.llançamentEnemicDetectat && estat.escuts > 0) {
            if (estat.distanciaLlançamentEnemic < 30) {
                activaEscut();
            }
        }
    }

    /**
     * metode que dispara a l'enemic de manera controlada, i inicia una
     * persecució
     */
    private void disparaEnemic() {
        if ((estat.veigAlgunEnemic) && (estat.llançaments != 0)) {
            for (int i = 0; estat.objectes[i] != null && i < estat.objectes.length; i++) {
                if (estat.objectes[i].agafaTipus() == Estat.AGENT && estat.objectes[i].agafaDistancia() < 120) {
                    if (nLlançaments < 3 && pausaDispar == 0) {
                        mira(estat.objectes[i]);
                        llança();
                        nLlançaments++;
                    } else {
                        mira(estat.objectes[i]);
                        iEnemic = i;
                        nLlançaments = 0;
                        pausaDispar = 30;
                    }
                }
            }
        }
    }

    /**
     * metode que utlitza un hiperespai si te poca vida i veu un enemic 
     */
    private void salvacio() {
        if (estat.impactesRebuts >= 3 && estat.forces < 5000 && estat.veigAlgunEnemic) {
            if (estat.hiperespais > 0) {
                hyperespai();
                if (estat.escuts > 0) {
                    activaEscut();
                }
            } else {
                activaEscut();
            }
        }
    }

    /**
     * metode que utlitza un hiperespai si no veu recursos durant un temps
     */
    private void novaUbicacio() {
        if (capObjectes > 100) {
            if (estat.hiperespais > 0) {
                hyperespai();
                if (estat.escuts > 0) {
                    activaEscut();
                }
            } else {
                activaEscut();
            }
            capObjectes = -1000;
        }
    }
}
