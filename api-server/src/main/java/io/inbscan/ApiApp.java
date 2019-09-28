package io.inbscan;

import io.inbscan.route.AccountRoutes;
import io.inbscan.route.BlockRoutes;
import io.inbscan.route.TransactionRoutes;
import org.jooby.Jooby;
import org.jooby.apitool.ApiTool;
import org.jooby.flyway.Flywaydb;
import org.jooby.jdbc.Jdbc;
import org.jooby.jooq.jOOQ;
import org.jooby.json.Jackson;
import org.jooq.DSLContext;
import org.jooq.conf.Settings;

public class ApiApp extends Jooby {

	{


		use(new Jdbc());
		use(new jOOQ());

		use(new Jackson());
		use(new Flywaydb());

		
		
		use("*", (req,res,chain)->{

			//CORS
			
			res.header("Access-Control-Allow-Origin", "*");
			res.header("Access-Control-Allow-Headers", "origin, content-type, accept");
			
			if (req.method().equals("OPTIONS")) {
				res.send(true);
				return;
			}

			
			chain.next(req, res);

			
		});
		
		onStart(registry -> {
			
			DSLContext dslContext = registry.require(DSLContext.class);

			Settings settings = new Settings();
			settings.setRenderSchema(false);

			dslContext.configuration().set(settings);

		});

		get("/",(req,res)->{
			res.redirect("/doc");
		});
		
		// routes

		use(BlockRoutes.class);
		use(AccountRoutes.class);
		use(TransactionRoutes.class);
		
		use(new ApiTool()
				 .filter(route -> {
				      return route.pattern().startsWith("/v1");
			    })
				.raml("/refactor").swagger("/navigate"));

	}

	public static void main(final String... args) {
		run(ApiApp::new, args);
	}

}
