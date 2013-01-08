package bootstrap.liftweb

import net.liftweb.http.LiftRules
import net.liftweb.sitemap._

import shiro.sitemap.{ DefaultLogin }
import shiro.{ Shiro, UrlConfig }

class Boot {
  def boot {

    object conf extends UrlConfig {
      override val baseURL = new FactoryMaker[Path](Nil){}
      override val loginURL = new FactoryMaker[Path]("login" :: Nil){}
    }

    val shiro = new Shiro( conf )

    shiro.init();
    
    LiftRules.addToPackages("eu.getintheloop")
    
    LiftRules.setSiteMap(SiteMap(List(
      Menu("Home") / "index" >> shiro.locs.RequireAuthentication,
      Menu("Role Test") / "restricted" >> shiro.locs.RequireAuthentication >> shiro.locs.HasRole("admin"),
      Menu("Login") / "login" >> DefaultLogin >> shiro.locs.RequireNoAuthentication
      ) ::: shiro.menus: _*
    ))
  }
}
