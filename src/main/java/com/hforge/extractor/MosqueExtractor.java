package com.hforge.extractor;

import com.hforge.config.GeneratorOptions;
import com.hforge.exceptions.ExtractorException;
import com.hforge.model.Mosque;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.pbf.seq.PbfIterator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extrait toutes les mosquées (nodes, ways, relations) d'un fichier .osm.pbf
 * et les exporte en CSV.
 *
 * Une mosquée est un objet OSM avec :
 *  - building=mosque, ou
 *  - amenity=mosque (ancien tag), ou
 *  - amenity=place_of_worship + religion=muslim
 *
 * Le fichier PBF doit être trié (nodes, puis ways, puis relations),
 * ce qui est le cas des extraits Geofabrik / planet.
 */
public class MosqueExtractor extends AbstractExtractor<Mosque> {

    // Rectangle englobant approximatif du Cameroun
    private static final double MIN_LAT = 1.65;
    private static final double MAX_LAT = 13.10;
    private static final double MIN_LON = 8.45;
    private static final double MAX_LON = 16.20;

    @Override
    protected String defaultOutputFile() {
        return "mosques_cameroun.csv";
    }

    // ------------------------------------------------------------------
    // Lecture du PBF
    // ------------------------------------------------------------------

    @Override
    protected List<Mosque> read(File input, GeneratorOptions options) {

        boolean verbose = options.isVerbose();
        boolean bbox = options.isBboxFilter();

        List<Mosque> result = new ArrayList<>();

        // mosquées de type way / relation : coordonnées calculées plus tard
        Map<Long, Mosque> wayMosques = new LinkedHashMap<>();
        Map<Long, Mosque> relationMosques = new LinkedHashMap<>();

        Map<Long, long[]> wayNodeIds = new HashMap<>();
        Map<Long, List<Long>> relationWayIds = new HashMap<>();

        try {

            // ---------- Passe 1 : repérer les mosquées ----------
            log(verbose, "Passe 1/3 : recherche des mosquées...");

            try (InputStream in = open(input)) {

                OsmIterator it = new PbfIterator(in, false);

                while (it.hasNext()) {

                    EntityContainer container = it.next();
                    OsmEntity entity = container.getEntity();

                    if (entity.getNumberOfTags() == 0) {
                        continue;
                    }

                    Map<String, String> tags = OsmModelUtil.getTagsAsMap(entity);

                    if (!isMosque(tags)) {
                        continue;
                    }

                    EntityType type = container.getType();

                    if (type == EntityType.Node) {

                        OsmNode node = (OsmNode) entity;

                        Mosque m = toMosque("node", node.getId(), tags);
                        m.setLatitude(node.getLatitude());
                        m.setLongitude(node.getLongitude());

                        if (!bbox || inBbox(m.getLatitude(), m.getLongitude())) {
                            result.add(m);
                        }

                    } else if (type == EntityType.Way) {

                        OsmWay way = (OsmWay) entity;

                        long[] ids = new long[way.getNumberOfNodes()];
                        for (int i = 0; i < ids.length; i++) {
                            ids[i] = way.getNodeId(i);
                        }

                        wayNodeIds.put(way.getId(), ids);
                        wayMosques.put(way.getId(), toMosque("way", way.getId(), tags));

                    } else if (type == EntityType.Relation) {

                        OsmRelation relation = (OsmRelation) entity;

                        List<Long> members = new ArrayList<>();
                        for (int i = 0; i < relation.getNumberOfMembers(); i++) {
                            OsmRelationMember member = relation.getMember(i);
                            if (member.getType() == EntityType.Way) {
                                members.add(member.getId());
                            }
                        }

                        relationWayIds.put(relation.getId(), members);
                        relationMosques.put(relation.getId(),
                                toMosque("relation", relation.getId(), tags));
                    }
                }
            }

            // Une way membre d'une relation mosquée est déjà couverte par la relation
            Set<Long> relationMemberWays = new HashSet<>();
            relationWayIds.values().forEach(relationMemberWays::addAll);
            relationMemberWays.forEach(wayMosques::remove);

            // ---------- Passe 2 : géométrie des ways membres de relations ----------
            Set<Long> missingWays = new HashSet<>(relationMemberWays);
            missingWays.removeAll(wayNodeIds.keySet());

            if (!missingWays.isEmpty()) {

                log(verbose, "Passe 2/3 : lecture des ways membres de relations...");

                try (InputStream in = open(input)) {

                    OsmIterator it = new PbfIterator(in, false);

                    while (it.hasNext()) {

                        EntityContainer container = it.next();

                        if (container.getType() == EntityType.Relation) {
                            break; // fichier trié : plus de ways après
                        }

                        if (container.getType() != EntityType.Way) {
                            continue;
                        }

                        OsmWay way = (OsmWay) container.getEntity();

                        if (missingWays.contains(way.getId())) {

                            long[] ids = new long[way.getNumberOfNodes()];
                            for (int i = 0; i < ids.length; i++) {
                                ids[i] = way.getNodeId(i);
                            }

                            wayNodeIds.put(way.getId(), ids);
                        }
                    }
                }
            }

            // ---------- Passe 3 : coordonnées des nodes nécessaires ----------
            Set<Long> neededNodes = new HashSet<>();

            for (Mosque m : wayMosques.values()) {
                addAll(neededNodes, wayNodeIds.get(m.getOsmId()));
            }
            for (List<Long> wayIds : relationWayIds.values()) {
                for (Long wayId : wayIds) {
                    addAll(neededNodes, wayNodeIds.get(wayId));
                }
            }

            Map<Long, double[]> coords = new HashMap<>();

            if (!neededNodes.isEmpty()) {

                log(verbose, "Passe 3/3 : lecture de " + neededNodes.size()
                        + " nodes...");

                try (InputStream in = open(input)) {

                    OsmIterator it = new PbfIterator(in, false);

                    while (it.hasNext()) {

                        EntityContainer container = it.next();

                        if (container.getType() != EntityType.Node) {
                            break; // fichier trié : plus de nodes après
                        }

                        OsmNode node = (OsmNode) container.getEntity();

                        if (neededNodes.contains(node.getId())) {
                            coords.put(node.getId(),
                                    new double[]{node.getLatitude(), node.getLongitude()});
                        }
                    }
                }
            }

            // ---------- Centroïdes ----------
            for (Mosque m : wayMosques.values()) {

                double[] c = centroid(List.of(m.getOsmId()), wayNodeIds, coords);

                if (c != null) {
                    m.setLatitude(c[0]);
                    m.setLongitude(c[1]);
                    if (!bbox || inBbox(c[0], c[1])) {
                        result.add(m);
                    }
                }
            }

            for (Map.Entry<Long, Mosque> e : relationMosques.entrySet()) {

                double[] c = centroid(relationWayIds.get(e.getKey()), wayNodeIds, coords);

                if (c != null) {
                    Mosque m = e.getValue();
                    m.setLatitude(c[0]);
                    m.setLongitude(c[1]);
                    if (!bbox || inBbox(c[0], c[1])) {
                        result.add(m);
                    }
                }
            }

        } catch (IOException e) {
            throw new ExtractorException(
                    "Erreur lecture PBF : " + input.getAbsolutePath(), e);
        }

        log(verbose, result.size() + " mosquée(s) retenue(s).");

        return result;
    }

    // ------------------------------------------------------------------
    // Colonnes CSV
    // ------------------------------------------------------------------

    @Override
    protected List<String> header() {
        return List.of(
                "osm_type", "osm_id", "name", "name_fr", "name_en", "name_ar",
                "alt_name", "denomination", "building", "city", "street",
                "house_number", "postcode", "phone", "website", "opening_hours",
                "wikidata", "latitude", "longitude", "osm_url"
        );
    }

    @Override
    protected List<String> row(Mosque m) {
        return List.of(
                nv(m.getOsmType()),
                String.valueOf(m.getOsmId()),
                nv(m.getName()),
                nv(m.getNameFr()),
                nv(m.getNameEn()),
                nv(m.getNameAr()),
                nv(m.getAltName()),
                nv(m.getDenomination()),
                nv(m.getBuilding()),
                nv(m.getCity()),
                nv(m.getStreet()),
                nv(m.getHouseNumber()),
                nv(m.getPostcode()),
                nv(m.getPhone()),
                nv(m.getWebsite()),
                nv(m.getOpeningHours()),
                nv(m.getWikidata()),
                String.format(java.util.Locale.ROOT, "%.7f", m.getLatitude()),
                String.format(java.util.Locale.ROOT, "%.7f", m.getLongitude()),
                "https://www.openstreetmap.org/" + m.getOsmType() + "/" + m.getOsmId()
        );
    }

    // ------------------------------------------------------------------
    // Utilitaires
    // ------------------------------------------------------------------

    private static boolean isMosque(Map<String, String> tags) {

        if ("mosque".equals(tags.get("building"))) {
            return true;
        }

        String amenity = tags.get("amenity");

        if ("mosque".equals(amenity)) {
            return true;
        }

        return "place_of_worship".equals(amenity)
                && "muslim".equalsIgnoreCase(tags.get("religion"));
    }

    private static Mosque toMosque(String type, long id, Map<String, String> tags) {

        Mosque m = new Mosque();

        m.setOsmType(type);
        m.setOsmId(id);
        m.setName(tags.get("name"));
        m.setNameFr(tags.get("name:fr"));
        m.setNameEn(tags.get("name:en"));
        m.setNameAr(tags.get("name:ar"));
        m.setAltName(tags.get("alt_name"));
        m.setDenomination(tags.get("denomination"));
        m.setBuilding(tags.get("building"));
        m.setCity(tags.get("addr:city"));
        m.setStreet(tags.get("addr:street"));
        m.setHouseNumber(tags.get("addr:housenumber"));
        m.setPostcode(tags.get("addr:postcode"));
        m.setPhone(firstNonNull(tags.get("phone"), tags.get("contact:phone")));
        m.setWebsite(firstNonNull(tags.get("website"), tags.get("contact:website")));
        m.setOpeningHours(tags.get("opening_hours"));
        m.setWikidata(tags.get("wikidata"));

        return m;
    }

    /** Moyenne des sommets de toutes les ways données (null si aucune coordonnée). */
    private static double[] centroid(List<Long> wayIds,
                                     Map<Long, long[]> wayNodeIds,
                                     Map<Long, double[]> coords) {

        double lat = 0;
        double lon = 0;
        int count = 0;

        for (Long wayId : wayIds) {

            long[] ids = wayNodeIds.get(wayId);

            if (ids == null) {
                continue;
            }

            // ways fermées : le dernier node répète le premier
            int end = (ids.length > 1 && ids[0] == ids[ids.length - 1])
                    ? ids.length - 1
                    : ids.length;

            for (int i = 0; i < end; i++) {

                double[] c = coords.get(ids[i]);

                if (c != null) {
                    lat += c[0];
                    lon += c[1];
                    count++;
                }
            }
        }

        return count == 0 ? null : new double[]{lat / count, lon / count};
    }

    private static boolean inBbox(double lat, double lon) {
        return lat >= MIN_LAT && lat <= MAX_LAT
                && lon >= MIN_LON && lon <= MAX_LON;
    }

    private static void addAll(Set<Long> target, long[] ids) {
        if (ids != null) {
            for (long id : ids) {
                target.add(id);
            }
        }
    }

    private static InputStream open(File file) throws IOException {
        return new BufferedInputStream(new FileInputStream(file), 1 << 16);
    }

    private static String nv(String value) {
        return value == null ? "" : value;
    }

    private static String firstNonNull(String a, String b) {
        return a != null ? a : b;
    }

    private static void log(boolean verbose, String message) {
        if (verbose) {
            System.out.println(message);
        }
    }

}
