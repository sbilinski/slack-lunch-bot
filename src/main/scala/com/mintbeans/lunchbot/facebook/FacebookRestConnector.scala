package com.mintbeans.lunchbot.facebook

import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import java.time.{LocalDateTime, ZoneId}

import com.restfb.Version.VERSION_2_3
import com.restfb._
import com.restfb.types.Post

import scala.collection.JavaConverters._

class FacebookRestConnector(appId: String, appSecret: String) extends DefaultFacebookClient(VERSION_2_3) with Facebook {

  accessToken = obtainAppAccessToken(appId, appSecret).getAccessToken

  override def lastPost(page: Facebook.Page, since: Option[LocalDateTime] = None): Option[Facebook.Post] = {
    val feed = since match {
      case None => fetchConnection(s"${page.id}/posts", classOf[Post])
      case Some(date) => fetchConnection(s"${page.id}/posts", classOf[Post],
                                         Parameter.`with`("since", date.format(ISO_LOCAL_DATE_TIME)),
                                         Parameter.`with`("fields", List("id", "message", "created_time", "full_picture").asJava))
    }

    feed.getData.asScala.toList.headOption match {
      case None => None
      case Some(post) => {
        val time = post.getCreatedTime.toInstant.atZone(ZoneId.systemDefault).toLocalDateTime
        val msg  = post.getMessage
        val pic  = post.getFullPicture

        Some(Facebook.Post(time, Option(msg), Option(pic)))
      }
    }
  }

}
