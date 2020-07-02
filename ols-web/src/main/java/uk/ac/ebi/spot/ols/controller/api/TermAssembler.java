package uk.ac.ebi.spot.ols.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.spot.ols.neo4j.model.OlsRelated;
import uk.ac.ebi.spot.ols.neo4j.model.OlsTerm;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Simon Jupp
 * @date 23/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class TermAssembler implements RepresentationModelAssembler<OlsTerm, EntityModel<OlsTerm>> {

    @Autowired
    EntityLinks entityLinks;

    @Override
    public EntityModel<OlsTerm> toModel(OlsTerm term) {
        EntityModel<OlsTerm> resource = new EntityModel<OlsTerm>(term);

        String id = UriUtils.encode(term.getIri(), "UTF-8");
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(OntologyTermController.class).getTerm(term.getOntologyName(), id));

        resource.add(lb.withSelfRel());

        if (!term.isRoot()) {
            resource.add(lb.slash("parents").withRel("parents"));
            resource.add(lb.slash("ancestors").withRel("ancestors"));
            resource.add(lb.slash("hierarchicalParents").withRel("hierarchicalParents"));
            resource.add(lb.slash("hierarchicalAncestors").withRel("hierarchicalAncestors"));
            resource.add(lb.slash("jstree").withRel("jstree"));
        }

        if (term.hasChildren()) {
            resource.add(lb.slash("children").withRel("children"));
            resource.add(lb.slash("descendants").withRel("descendants"));
            resource.add(lb.slash("hierarchicalChildren").withRel("hierarchicalChildren"));
            resource.add(lb.slash("hierarchicalDescendants").withRel("hierarchicalDescendants"));
        }

        resource.add(lb.slash("graph").withRel("graph"));

        Collection<String> relation = new HashSet<>();
        for (OlsRelated related : term.getRelated()) {
            if (!relation.contains(related.getLabel())) {
                String relationId = UriUtils.encode(related.getUri(), "UTF-8");

                resource.add(lb.slash(relationId).withRel(related.getLabel().replaceAll(" ", "_")));
            }
            relation.add(related.getLabel());
        }

//        resource.add(lb.slash("related").withRel("related"));
        // other links

        return resource;
    }
}