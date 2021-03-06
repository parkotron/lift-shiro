package shiro.sitemap

import net.liftweb.http.{ S, Factory }
import shiro.{ Shiro, LoginRedirect, UrlConfig }

/**
 * Lift SiteMap Integration
 */
import net.liftweb.sitemap.Loc.{If,EarlyResponse,DispatchLocSnippets}
object DefaultLogin
    extends DispatchLocSnippets 
    with shiro.snippet.DefaultUsernamePasswordLogin { 
    def dispatch = { 
      case "login" => render 
    }
  }

class Locs( config:UrlConfig ) {

  private val injectConfig = config

  import net.liftweb.common.Full
  import net.liftweb.http.{RedirectResponse, RedirectWithState, S, RedirectState}
  import net.liftweb.sitemap.{Menu,Loc}
  import net.liftweb.sitemap.Loc.{If,EarlyResponse,DispatchLocSnippets}
  import shiro.Utils._
  
  implicit def listToPath(in: List[String]): String = in.mkString("/","/","")
  
  //private val loginURL = Shiro.baseURL.vend ::: Shiro.loginURL.vend
  //private val indexURL = Shiro.baseURL.vend ::: Shiro.indexURL.vend
  //private val logoutURL = Shiro.baseURL.vend ::: Shiro.logoutURL.vend

  private val loginURL = injectConfig.baseURL.vend ::: injectConfig.loginURL.vend
  private val indexURL = injectConfig.baseURL.vend ::: injectConfig.indexURL.vend
  private val logoutURL = injectConfig.baseURL.vend ::: injectConfig.logoutURL.vend
  
  def RedirectBackToReferrer = {
    val uri = S.uriAndQueryString
    RedirectWithState(loginURL, RedirectState(() => { LoginRedirect.set(uri) }))
  }
  
  def RedirectToIndexURL = RedirectResponse(indexURL)
  
  private def DisplayError(message: String) = () => 
    RedirectWithState(loginURL, RedirectState(() => S.error(message)))
  
  def RequireAuthentication = If(
    () => isAuthenticated, 
    () => RedirectBackToReferrer)
  
  def RequireNoAuthentication = If(
    () => !isAuthenticated,
    () => RedirectToIndexURL)
  
  def RequireRemembered = If(
    () => isAuthenticatedOrRemembered,
    () => RedirectBackToReferrer)
  
  def RequireNotRemembered = If(
    () => !isAuthenticatedOrRemembered,
    () => RedirectToIndexURL)
  
  def logoutMenu = Menu(Loc("Logout", logoutURL, 
    S.?("logout"), logoutLocParams))
  
  private val logoutLocParams = RequireRemembered :: 
    EarlyResponse(() => {
        if(isAuthenticatedOrRemembered){ subject.logout() }
      Full(RedirectResponse(injectConfig.indexURL.vend))
    }) :: Nil
  
  def HasRole(role: String) = 
    If(() => hasRole(role), 
      DisplayError("You are the wrong role to access that resource."))
  
  def LacksRole(role: String) = 
    If(() => lacksRole(role),
      DisplayError("You lack the sufficient role to access that resource."))
  
  def HasPermission(permission: String) = 
    If(() => hasPermission(permission), 
      DisplayError("Insufficient permissions to access that resource."))

  def LacksPermission(permission: String) = 
    If(() => lacksPermission(permission), 
      DisplayError("Overqualified permissions to access that resource."))
 
  def HasAnyRoles(roles: Seq[String]) = 
    If(() => hasAnyRoles(roles),
       DisplayError("You are the wrong role to access that resource."))

  def IsRunAs(roles: Seq[String]) = 
    If(() => isRunAs(roles), 
      DisplayError("You need to take the role of another user to access that resource."))
}
