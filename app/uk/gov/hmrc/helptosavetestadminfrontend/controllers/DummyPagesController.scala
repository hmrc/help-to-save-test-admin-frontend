
package uk.gov.hmrc.helptosavetestadminfrontend.controllers

import com.google.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.helptosavetestadminfrontend.views.html._

@Singleton
class DummyPagesController @Inject() (mcc: MessagesControllerComponents,
                                      account_homepage: account_homepage,
                                      pay_in: pay_in
                                     ) extends FrontendController(mcc) with I18nSupport {

  def accountHomepage: Action[AnyContent] = Action { implicit request ⇒
    Ok(account_homepage())
  }


  def payIn: Action[AnyContent] = Action { implicit request ⇒
    Ok(pay_in())
  }

}