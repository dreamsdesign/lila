package lila.notify

import lila.common.LightUser
import play.api.libs.json._

final class JSONHandlers(
    getLightUser: LightUser.Getter) {

  implicit val privateMessageThreadWrites = Json.writes[PrivateMessage.Thread]
  implicit val qaQuestionWrites = Json.writes[QaAnswer.Question]

  implicit val notificationWrites: Writes[Notification] = new Writes[Notification] {
    def writeBody(notificationContent: NotificationContent) = {
      notificationContent match {
        case MentionedInThread(mentionedBy, topic, _, category, postId) => Json.obj(
          "mentionedBy" -> getLightUser(mentionedBy.value),
          "topic" -> topic.value, "category" -> category.value,
          "postId" -> postId.value)
        case InvitedToStudy(invitedBy, studyName, studyId) => Json.obj(
          "invitedBy" -> getLightUser(invitedBy.value),
          "studyName" -> studyName.value,
          "studyId" -> studyId.value)
        case PrivateMessage(senderId, thread, text) => Json.obj(
          "sender" -> getLightUser(senderId.value),
          "thread" -> privateMessageThreadWrites.writes(thread),
          "text" -> text.value)
        case QaAnswer(answeredBy, question, answerId) => Json.obj(
          "answerer" -> getLightUser(answeredBy.value),
          "question" -> question,
          "answerId" -> answerId.value)
        case TeamJoined(id, name) => Json.obj(
          "id" -> id.value,
          "name" -> name.value)
        case NewBlogPost(id, slug, title) => Json.obj(
          "id" -> id.value,
          "slug" -> slug.value,
          "title" -> title.value)
        case AnalysisFinished(id, color, against) => Json.obj(
          "id" -> id.value,
          "color" -> color.name,
          "opponentName" -> against.value)
      }
    }

    def writes(notification: Notification) = {
      val body = notification.content

      val notificationType = body match {
        case _: MentionedInThread => "mentioned"
        case _: InvitedToStudy    => "invitedStudy"
        case _: PrivateMessage    => "privateMessage"
        case _: QaAnswer          => "qaAnswer"
        case _: TeamJoined        => "teamJoined"
        case _: NewBlogPost       => "newBlogPost"
        case _: AnalysisFinished  => "analysisFinished"
      }

      Json.obj("content" -> writeBody(body),
        "type" -> notificationType,
        "read" -> notification.read.value,
        "date" -> notification.createdAt)
    }
  }

  import lila.common.paginator.PaginatorJson._
  implicit val unreadWrites = Writes[Notification.UnreadCount] { v => JsNumber(v.value) }
  implicit val andUnreadWrites: Writes[Notification.AndUnread] = Json.writes[Notification.AndUnread]

  implicit val newNotificationWrites: Writes[NewNotification] = new Writes[NewNotification] {

    def writes(newNotification: NewNotification) = {
      Json.obj("notification" -> newNotification.notification, "unread" -> newNotification.unreadNotifications)
    }
  }
}

