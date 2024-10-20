<template>
  <a
    v-if="id"
    :id="componentId"
    :title="id"
    :href="url"
    rel="nofollow"
    target="_blank">
    <span v-text="name"></span>
  </a>
</template>

<script>
export default {
  props: {
    technicalId: {
      type: String,
      default: function() {
        return null;
      }
    },
    name: {
      type: String,
      default: function() {
        return null;
      }
    },
    id: {
      type: String,
      default: function() {
        return null;
      }
    },
    type: {
      type: String,
      default: function() {
        return null;
      }
    }
  },
  data () {
    return {
      componentId: `ReceiverLink${parseInt(Math.random() * 10000).toString().toString()}`,
    };
  },
  computed: {
    url() {
      if (this.type === 'user' || this.type === 'organization') {
        return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.id}`;
      } else {
        return `${eXo.env.portal.context}/s/${this.technicalId}`;
      }
    }
  },
};
</script>
