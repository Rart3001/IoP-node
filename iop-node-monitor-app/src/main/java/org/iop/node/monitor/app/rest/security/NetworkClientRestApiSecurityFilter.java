package org.iop.node.monitor.app.rest.security;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.util.GsonProvider;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.daos.JPADaoFactory;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.ClientSession;
import com.google.gson.Gson;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * The Class <code>SecurityFilter</code> implements
 * <p/>
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 08/07/16.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public class NetworkClientRestApiSecurityFilter implements Filter {

    /**
     * Represent the logger instance
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(NetworkClientRestApiSecurityFilter.class));

    /**
     * Represent the gson
     */
    private final Gson gson;

    /**
     * Constructor
     */
    public NetworkClientRestApiSecurityFilter(){
        super();
        this.gson = GsonProvider.getGson();
    }

    /**
     * (non-javadoc)
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("init");
    }

    /**
     * (non-javadoc)
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        LOG.debug("doFilter");

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {

            LOG.debug("Authorization = " + httpRequest.getHeader("Authorization"));

            final String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.contains("Bearer ")) {
                LOG.error("Missing or invalid Authorization header.");
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header.");
                return;
            }

            String clientIdentityPublicKey = authHeader.substring("Bearer ".length(), authHeader.length());
            LOG.debug("clientIdentityPublicKey = " + clientIdentityPublicKey);

            List<ClientSession> checkedInProfile = JPADaoFactory.getClientSessionDao().list("client.id", clientIdentityPublicKey);
            LOG.debug("checkedInProfile = " + checkedInProfile);

            if (!checkedInProfile.isEmpty()){
                chain.doFilter(request, response);
            }else {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Client Identity PublicKey not Authorize.");
            }

        } catch (final Exception e) {
            LOG.error( "Error in token: "+e.getMessage());
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: "+e.getMessage());
        }
    }

    /**
     * (non-javadoc)
     * @see Filter#destroy()
     */
    @Override
    public void destroy() {
        LOG.info("destroy");
    }
}
