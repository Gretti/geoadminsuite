package fr.sie.brique.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.sie.brique.model.Line;
import fr.sie.brique.model.Point;
import fr.sie.brique.model.Polygon;
import fr.sie.brique.web.action.GeobsResponse;

/**
 * Service permettant de simplifier les méthodes les plus utiles côté DAO.
 *
 * @author mauclerc
 */
public class DAOService {

    public static final Logger LOGGER = Logger.getLogger("fr.brgm.util");

    /**
     * Modifier un point.
     *
     * @param pt Point a modifier.
     * @return
     */
    public static GeobsResponse modifyPoint(Point pt) {
        GeobsResponse gResponse = new GeobsResponse();
        // Création des DAO nécessaires
        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        try {
            // Récupération d'une connexion
            Connection conn = pgFactory.getConnection();
            try {
                conn.setAutoCommit(false);
                // Mise à jour en base
                DAOService.updatePoint(pt, gResponse, conn);
                conn.commit();

                conn.setAutoCommit(true);
                gResponse.setCloseDialog(true);

            } catch (SQLException exception) {
                exception.printStackTrace();
                conn.rollback();
                LOGGER.severe("Erreur lors de la transaction. Rollback !");
                gResponse.setResultCode(1);
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (SQLException exception) {
            LOGGER.severe("Erreur grave lors de la manipulation de la connexion.");
        }

        return gResponse;
    }

    /**
     * Creer un point.
     *
     * @param pt Le point a creer.
     * @return
     */
    public static GeobsResponse createPoint(Point pt) {
        GeobsResponse gResponse = new GeobsResponse();
        // Création des DAO nécessaires
        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        try {
            // Récupération d'une connexion
            Connection conn = pgFactory.getConnection();
            try {
                conn.setAutoCommit(false);

                PointDAO ptDAO = pgFactory.getPointDAO();
                Integer id = ptDAO.insert(pt, conn);

                gResponse.addIdParToAdd(id);
                conn.commit();
                conn.setAutoCommit(true);
                gResponse.setCloseDialog(true);

            } catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
                LOGGER.severe("Erreur lors de la transaction. Rollback !");
                gResponse.setResultCode(1);
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (SQLException exception) {
            LOGGER.severe("Erreur grave lors de la manipulation de la connexion.");
        }
        return gResponse;
    }

    /**
     * Met à jour un point passé et complète la réponse avec les ids à ajouter
     * ou supprimer de la sélection courante.
     *
     * @param pt Point à mettre à jour
     * @param gResponse Réponse à compléter
     * @throws SQLException
     */
    private static void updatePoint(Point pt, GeobsResponse gResponse,
            Connection conn) throws SQLException {
        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        PointDAO ptDAO = pgFactory.getPointDAO();
        Boolean succeed = ptDAO.update(pt, conn);
        if (succeed) {
            gResponse.addIdRefToAdd(pt.getGid());
        } else {
            LOGGER.log(Level.SEVERE, "La mise à jour a échoué.");
        }

        if (!succeed) {
            gResponse.setResultCode(1);
        }
    }

    /**
     * Supprimer des points.
     *
     * @param ids Liste des id des points.
     * @return
     */
    public static GeobsResponse deletePoints(List<String> ids) {
        GeobsResponse gResponse = new GeobsResponse();
        // Création des DAO nécessaires
        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        PointDAO ptDAO = pgFactory.getPointDAO();
        try {
            // Récupération d'une connexion
            Connection conn = pgFactory.getConnection();
            try {
                conn.setAutoCommit(false);

                // Traitement des obstacles à supprimer
                for (String ptId : ids) {
                    Point pt = ptDAO.getPointById(ptId, conn);
                    if (pt != null) {
                        // On supprime l'obstacle référentiel en cascade
                        DAOService.deletePoint(pt, gResponse, conn);
                    }
                }

                conn.commit();
                conn.setAutoCommit(true);
                gResponse.setCloseDialog(true);

            } catch (SQLException exception) {
                conn.rollback();
                LOGGER.severe("Erreur lors de la transaction. Rollback !");
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (SQLException exception) {
            LOGGER.severe("Erreur grave lors de la manipulation de la connexion.");
        }

        return gResponse;
    }

    /**
     * Supprime le point passé.
     *
     * @param pt Point à supprimer
     * @param gResponse Réponse à mettre à jour
     * @throws SQLException
     */
    private static void deletePoint(Point pt, GeobsResponse gResponse,
            Connection conn) throws SQLException {

        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        PointDAO ptDAO = pgFactory.getPointDAO();
        // On supprime en cascade l'obstacle referentiel de la table
        Integer ptId = ptDAO.delete(pt, conn);

        if (ptId != null) {
            gResponse.addIdRefToDel(ptId);
        }

        if (ptId == null) {
            gResponse.setResultCode(1);
        }
    }

    /**
     * Modifier une ligne.
     *
     * @param line Ligne a modifier.
     * @return
     */
    public static GeobsResponse modifyLine(Line line) {
        GeobsResponse gResponse = new GeobsResponse();
        // Création des DAO nécessaires
        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        try {
            // Récupération d'une connexion
            Connection conn = pgFactory.getConnection();
            try {
                conn.setAutoCommit(false);
                // Mise à jour en base
                DAOService.updateLine(line, gResponse, conn);
                conn.commit();

                conn.setAutoCommit(true);
                gResponse.setCloseDialog(true);

            } catch (SQLException exception) {
                conn.rollback();
                LOGGER.severe("Erreur lors de la transaction. Rollback !");
                gResponse.setResultCode(1);
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (SQLException exception) {
            LOGGER.severe("Erreur grave lors de la manipulation de la connexion.");
        }

        return gResponse;
    }

    /**
     * Creer une ligne.
     *
     * @param line La ligne a creer.
     * @return
     */
    public static GeobsResponse createLine(Line line) {
        GeobsResponse gResponse = new GeobsResponse();
        // Création des DAO nécessaires
        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        try {
            // Récupération d'une connexion
            Connection conn = pgFactory.getConnection();
            try {
                conn.setAutoCommit(false);

                LineDAO lineDAO = pgFactory.getLineDAO();
                Integer id = lineDAO.insert(line, conn);

                gResponse.addIdParToAdd(id);
                conn.commit();
                conn.setAutoCommit(true);
                gResponse.setCloseDialog(true);

            } catch (SQLException exception) {
                conn.rollback();
                LOGGER.severe("Erreur lors de la transaction. Rollback !");
                gResponse.setResultCode(1);
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (SQLException exception) {
            LOGGER.severe("Erreur grave lors de la manipulation de la connexion.");
        }
        return gResponse;
    }

    /**
     * Met à jour la ligne passé.
     *
     * @param pt Ligne à mettre à jour
     * @param gResponse Réponse à compléter
     * @throws SQLException
     */
    private static void updateLine(Line pt, GeobsResponse gResponse,
            Connection conn) throws SQLException {
        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        LineDAO lineDAO = pgFactory.getLineDAO();
        Boolean succeed = lineDAO.update(pt, conn);
        if (succeed) {
            gResponse.addIdRefToAdd(pt.getId_brique());
        } else {
            LOGGER.log(Level.SEVERE, "La mise à jour a échoué.");
        }

        if (!succeed) {
            gResponse.setResultCode(1);
        }
    }

    /**
     * Supprimer des lignes.
     *
     * @param ids Liste des id des lignes.
     * @return
     */
    public static GeobsResponse deleteLines(List<String> ids) {
        GeobsResponse gResponse = new GeobsResponse();
        // Création des DAO nécessaires
        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        LineDAO lineDAO = pgFactory.getLineDAO();
        try {
            // Récupération d'une connexion
            Connection conn = pgFactory.getConnection();
            try {
                conn.setAutoCommit(false);

                // Traitement des obstacles à supprimer
                for (String lineId : ids) {
                    Line line = lineDAO.getLineById(lineId, conn);
                    if (line != null) {
                        // On supprime l'obstacle référentiel en cascade
                        DAOService.deleteLine(line, gResponse, conn);
                    }
                }

                conn.commit();
                conn.setAutoCommit(true);
                gResponse.setCloseDialog(true);

            } catch (SQLException exception) {
                conn.rollback();
                LOGGER.severe("Erreur lors de la transaction. Rollback !");
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (SQLException exception) {
            LOGGER.severe("Erreur grave lors de la manipulation de la connexion.");
        }

        return gResponse;
    }

    /**
     * Supprime la ligne passé
     *
     * @param line Ligne à supprimer
     * @param gResponse Réponse à mettre à jour
     * @throws SQLException
     */
    private static void deleteLine(Line line, GeobsResponse gResponse,
            Connection conn) throws SQLException {

        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        LineDAO lineDAO = pgFactory.getLineDAO();
        // On supprime en cascade l'obstacle referentiel de la table
        Integer lineId = lineDAO.delete(line, conn);

        if (lineId != null) {
            gResponse.addIdRefToDel(lineId);
        }

        if (lineId == null) {
            gResponse.setResultCode(1);
        }
    }

    /**
     * Modifier un polygone.
     *
     * @param poly Polygone a modifier.
     * @return
     */
    public static GeobsResponse modifyPolygon(Polygon poly) {
        GeobsResponse gResponse = new GeobsResponse();
        // Création des DAO nécessaires
        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        try {
            // Récupération d'une connexion
            Connection conn = pgFactory.getConnection();
            try {
                conn.setAutoCommit(false);
                // Mise à jour en base
                DAOService.updatePolygon(poly, gResponse, conn);
                conn.commit();

                conn.setAutoCommit(true);
                gResponse.setCloseDialog(true);

            } catch (SQLException exception) {
                conn.rollback();
                LOGGER.severe("Erreur lors de la transaction. Rollback !");
                gResponse.setResultCode(1);
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (SQLException exception) {
            LOGGER.severe("Erreur grave lors de la manipulation de la connexion.");
        }

        return gResponse;
    }

    /**
     * Creer un polygone.
     *
     * @param poly Le polygone a creer.
     * @return
     */
    public static GeobsResponse createPolygon(Polygon poly) {
        GeobsResponse gResponse = new GeobsResponse();
        // Création des DAO nécessaires
        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        try {
            // Récupération d'une connexion
            Connection conn = pgFactory.getConnection();
            try {
                conn.setAutoCommit(false);

                PolygonDAO polyDAO = pgFactory.getPolygonDAO();
                Integer id = polyDAO.insert(poly, conn);

                gResponse.addIdParToAdd(id);
                conn.commit();
                conn.setAutoCommit(true);
                gResponse.setCloseDialog(true);

            } catch (SQLException exception) {
                conn.rollback();
                LOGGER.severe("Erreur lors de la transaction. Rollback !");
                gResponse.setResultCode(1);
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (SQLException exception) {
            LOGGER.severe("Erreur grave lors de la manipulation de la connexion.");
        }
        return gResponse;
    }

    /**
     * Met à jour le polygone passé
     *
     * @param pt Polygone à mettre à jour
     * @param gResponse Réponse à compléter
     * @throws SQLException
     */
    private static void updatePolygon(Polygon pt, GeobsResponse gResponse,
            Connection conn) throws SQLException {
        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        PolygonDAO polygonDAO = pgFactory.getPolygonDAO();
        Boolean succeed = polygonDAO.update(pt, conn);
        if (succeed) {
            gResponse.addIdRefToAdd(pt.getId_brique());
        } else {
            LOGGER.log(Level.SEVERE, "La mise à jour a échoué.");
        }

        if (!succeed) {
            gResponse.setResultCode(1);
        }
    }

    /**
     * Supprimer des polygones.
     *
     * @param ids Liste des id des polygones.
     * @return
     */
    public static GeobsResponse deletePolygons(List<String> ids) {
        GeobsResponse gResponse = new GeobsResponse();
        // Création des DAO nécessaires
        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        PolygonDAO polygonDAO = pgFactory.getPolygonDAO();
        try {
            // Récupération d'une connexion
            Connection conn = pgFactory.getConnection();
            try {
                conn.setAutoCommit(false);

                // Traitement des obstacles à supprimer
                for (String polyId : ids) {
                    Polygon poly = polygonDAO.getPolygonById(polyId, conn);
                    if (poly != null) {
                        // On supprime l'obstacle référentiel en cascade
                        DAOService.deletePolygon(poly, gResponse, conn);
                    }
                }

                conn.commit();
                conn.setAutoCommit(true);
                gResponse.setCloseDialog(true);

            } catch (SQLException exception) {
                conn.rollback();
                LOGGER.severe("Erreur lors de la transaction. Rollback !");
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (SQLException exception) {
            LOGGER.severe("Erreur grave lors de la manipulation de la connexion.");
        }

        return gResponse;
    }

    /**
     * Supprime le polygone passé.
     *
     * @param poly Polgone à supprimer
     * @param gResponse Réponse à mettre à jour
     * @throws SQLException
     */
    private static void deletePolygon(Polygon poly, GeobsResponse gResponse,
            Connection conn) throws SQLException {

        DAOFactory pgFactory = DAOFactory.getDAOFactory(DAOFactory.POSTGRES);
        PolygonDAO polygonDAO = pgFactory.getPolygonDAO();
        // On supprime en cascade l'obstacle referentiel de la table
        Integer polyId = polygonDAO.delete(poly, conn);

        if (polyId != null) {
            gResponse.addIdRefToDel(polyId);
        }

        if (polyId == null) {
            gResponse.setResultCode(1);
        }
    }
}
